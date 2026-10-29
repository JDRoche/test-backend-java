Feature: Cliente events published to RabbitMQ (asynchronous communication with MS Cuentas)

  Background:
    * url baseUrl
    * def clienteId = newClienteId()
    * def identificacion = newIdentificacion()
    * def queue = 'karate-' + java.util.UUID.randomUUID().toString().substring(0, 8)
    * def mgmt = rabbitMgmtUrl + '/api'
    # a private queue bound to the exchange plays the role of the consumer; the exchange is declared the same way the application does
    Given url mgmt + '/exchanges/%2F/cliente.events'
    And header Authorization = rabbitAuth
    And request { type: 'topic', durable: true, auto_delete: false }
    When method put
    Then assert responseStatus == 201 || responseStatus == 204
    Given url mgmt + '/queues/%2F/' + queue
    And header Authorization = rabbitAuth
    And request { durable: false, auto_delete: true }
    When method put
    Then assert responseStatus == 201 || responseStatus == 204
    Given url mgmt + '/bindings/%2F/e/cliente.events/q/' + queue
    And header Authorization = rabbitAuth
    And request { routing_key: 'cliente.#' }
    When method post
    Then status 201

  Scenario: Create, update and delete publish created, updated and deleted events in order
    Given url baseUrl
    And path 'api', 'clientes'
    And request { clienteId: '#(clienteId)', nombre: 'Evento Uno', identificacion: '#(identificacion)', contrasena: '1234' }
    When method post
    Then status 201
    Given url baseUrl
    And path 'api', 'clientes', clienteId
    And request { nombre: 'Evento Uno', identificacion: '#(identificacion)', estado: false }
    When method put
    Then status 200
    Given url baseUrl
    And path 'api', 'clientes', clienteId
    When method delete
    Then status 204
    # "ack_requeue_true" only peeks at the queue, so the polling can be repeated until the three events arrive
    * configure retry = { count: 20, interval: 500 }
    Given url mgmt + '/queues/%2F/' + queue + '/get'
    And header Authorization = rabbitAuth
    And request { count: 10, ackmode: 'ack_requeue_true', encoding: 'auto' }
    And retry until response.length >= 3
    When method post
    Then status 200
    And match response == '#[3]'
    And match response[*].routing_key == ['cliente.created', 'cliente.updated', 'cliente.deleted']
    * json created = response[0].payload
    * json updated = response[1].payload
    * json deleted = response[2].payload
    * match created == { eventId: '#uuid', occurredAt: '#string', clienteId: '#(clienteId)', nombre: 'Evento Uno', estado: true }
    * match updated == { eventId: '#uuid', occurredAt: '#string', clienteId: '#(clienteId)', nombre: 'Evento Uno', estado: false }
    * match deleted == { eventId: '#uuid', occurredAt: '#string', clienteId: '#(clienteId)' }

  Scenario: A rejected request publishes no event
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given url baseUrl
    And path 'api', 'clientes'
    And request { clienteId: '#(newClienteId())', nombre: 'Duplicado', identificacion: '#(identificacion)', contrasena: '1' }
    When method post
    Then status 409
    * java.lang.Thread.sleep(1500)
    Given url mgmt + '/queues/%2F/' + queue + '/get'
    And header Authorization = rabbitAuth
    And request { count: 10, ackmode: 'ack_requeue_true', encoding: 'auto' }
    When method post
    Then status 200
    And match response == '#[1]'
    And match response[0].routing_key == 'cliente.created'

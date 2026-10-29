Feature: Clientes (CRUD)

  Background:
    * url baseUrl
    * def clienteId = newClienteId()
    * def identificacion = newIdentificacion()

  Scenario: Create a cliente (8.1 - Jose Lema) and never expose its password
    Given path 'api', 'clientes'
    And request { clienteId: '#(clienteId)', nombre: 'Jose Lema', genero: 'MALE', edad: 35, identificacion: '#(identificacion)', direccion: 'Otavalo sn y principal', telefono: '098254785', contrasena: '1234' }
    When method post
    Then status 201
    And match response == { clienteId: '#(clienteId)', nombre: 'Jose Lema', genero: 'MALE', edad: 35, identificacion: '#(identificacion)', direccion: 'Otavalo sn y principal', telefono: '098254785', estado: true }
    And match response !contains { contrasena: '#notnull' }

  Scenario: The clienteId is generated when it is not sent
    Given path 'api', 'clientes'
    And request { nombre: 'Marianela Montalvo', identificacion: '#(identificacion)', contrasena: '5678' }
    When method post
    Then status 201
    And match response.clienteId == '#uuid'

  Scenario: A duplicated identificacion is rejected with 409
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given path 'api', 'clientes'
    And request { clienteId: '#(newClienteId())', nombre: 'Otro', identificacion: '#(identificacion)', contrasena: '1' }
    When method post
    Then status 409
    And match response.message contains 'Ya existe un cliente con identificación'

  Scenario: A duplicated clienteId is rejected with 409
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given path 'api', 'clientes'
    And request { clienteId: '#(clienteId)', nombre: 'Otro', identificacion: '#(newIdentificacion())', contrasena: '1' }
    When method post
    Then status 409

  Scenario: Missing required fields are rejected with 400 and a standard error body
    Given path 'api', 'clientes'
    And request { nombre: '', contrasena: '' }
    When method post
    Then status 400
    And match response == { timestamp: '#string', status: 400, error: 'Bad Request', message: '#string', path: '/api/clientes' }
    And match response.message contains 'nombre es obligatorio'
    And match response.message contains 'identificacion es obligatoria'
    And match response.message contains 'contrasena es obligatoria'

  Scenario: An invalid genero is rejected with 400
    Given path 'api', 'clientes'
    And request { nombre: 'X', genero: 'XX', identificacion: '#(identificacion)', contrasena: '1' }
    When method post
    Then status 400

  Scenario: An out of range edad is rejected with 400
    Given path 'api', 'clientes'
    And request { nombre: 'X', edad: 200, identificacion: '#(identificacion)', contrasena: '1' }
    When method post
    Then status 400
    And match response.message contains 'edad'

  Scenario: Get a cliente by clienteId and list the clientes
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given path 'api', 'clientes', clienteId
    When method get
    Then status 200
    And match response.clienteId == clienteId
    Given path 'api', 'clientes'
    When method get
    Then status 200
    And match response[*].clienteId contains clienteId

  Scenario: Getting an unknown cliente answers 404
    Given path 'api', 'clientes', 'no-existe'
    When method get
    Then status 404
    And match response.message == 'Cliente no encontrado: no-existe'

  Scenario: Update a cliente (PUT) and keep its password when none is sent
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given path 'api', 'clientes', clienteId
    And request { nombre: 'Nombre Editado', genero: 'OTHER', edad: 22, identificacion: '#(identificacion)', direccion: 'Nueva direccion', telefono: '090000000', estado: false }
    When method put
    Then status 200
    And match response contains { clienteId: '#(clienteId)', nombre: 'Nombre Editado', genero: 'OTHER', edad: 22, estado: false }

  Scenario: Updating a cliente with the identificacion of another one is rejected with 409
    * def otherId = newIdentificacion()
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    # assigned on purpose: a call without assignment shares its scope and its arguments would overwrite clienteId and identificacion
    * def other = call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(newClienteId())', identificacion: '#(otherId)' }
    Given path 'api', 'clientes', clienteId
    And request { nombre: 'X', identificacion: '#(otherId)', estado: true }
    When method put
    Then status 409

  Scenario: Updating an unknown cliente answers 404
    Given path 'api', 'clientes', 'no-existe'
    And request { nombre: 'X', identificacion: '#(identificacion)', estado: true }
    When method put
    Then status 404

  Scenario: Delete a cliente
    * call read('classpath:karate/support/create-cliente.feature') { clienteId: '#(clienteId)', identificacion: '#(identificacion)' }
    Given path 'api', 'clientes', clienteId
    When method delete
    Then status 204
    Given path 'api', 'clientes', clienteId
    When method get
    Then status 404

  Scenario: Deleting an unknown cliente answers 404
    Given path 'api', 'clientes', 'no-existe'
    When method delete
    Then status 404

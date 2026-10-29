@ignore
Feature: Publishes a cliente event to the cliente.events exchange (through the RabbitMQ management API)

  Scenario: publish
    * def routingKey = __arg.routingKey || 'cliente.created'
    * def eventId = java.util.UUID.randomUUID() + ''
    * def occurredAt = __arg.occurredAt || (java.time.Instant.now() + '')
    * string payload = { eventId: '#(eventId)', occurredAt: '#(occurredAt)', clienteId: '#(__arg.clienteId)', nombre: '#(__arg.nombre)', estado: '#(__arg.estado)' }
    * def body = { properties: { content_type: 'application/json' }, routing_key: '#(routingKey)', payload: '#(payload)', payload_encoding: 'string' }
    Given url rabbitMgmtUrl + '/api/exchanges/%2F/cliente.events/publish'
    And header Authorization = rabbitAuth
    And request body
    When method post
    Then status 200
    And match response.routed == true

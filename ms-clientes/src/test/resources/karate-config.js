function fn() {
  var baseUrl = karate.properties['baseUrl'] || 'http://localhost:8081';
  var rabbitMgmtUrl = karate.properties['rabbitMgmtUrl'] || 'http://localhost:15672';
  var user = karate.properties['rabbitUser'] || 'guest';
  var pass = karate.properties['rabbitPassword'] || 'guest';
  var JavaString = Java.type('java.lang.String');
  var credentials = new JavaString(user + ':' + pass).getBytes('UTF-8');
  var rabbitAuth = 'Basic ' + java.util.Base64.getEncoder().encodeToString(credentials);

  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 20000);

  return {
    baseUrl: baseUrl,
    rabbitMgmtUrl: rabbitMgmtUrl,
    rabbitAuth: rabbitAuth,
    newClienteId: function () { return 'k-' + java.util.UUID.randomUUID().toString().substring(0, 8); },
    newIdentificacion: function () { return '' + (1000000000 + Math.floor(Math.random() * 8999999999)); }
  };
}

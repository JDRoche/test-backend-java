function fn() {
  var baseUrl = karate.properties['baseUrl'] || 'http://localhost:8082';
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
    newNumero: function () { return '' + (100000 + Math.floor(Math.random() * 899999)); },
    newClienteId: function () { return 'k-' + java.util.UUID.randomUUID().toString().substring(0, 8); }
  };
}

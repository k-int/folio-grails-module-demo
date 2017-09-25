curl -i -w '\n' -X GET http://localhost:9130/_/proxy/tenants
curl -i -w '\n' -X GET http://localhost:9130/_/proxy/modules
curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @testlib-tenant.json http://localhost:9130/_/proxy/tenants
curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @ModuleDescriptor.json http://localhost:9130/_/proxy/modules
curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @DeploymentDescriptor.json http://localhost:9130/_/discovery/modules
curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @testlib-mod-activation.json http://localhost:9130/_/proxy/tenants/testlib/modules

echo Call hello
curl -i -w '\n' -X GET -H 'X-Okapi-Tenant: testlib' http://localhost:9130/hello

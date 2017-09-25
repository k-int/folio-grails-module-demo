#!/bin/sh

if [ $# -eq 0 ]
then
  echo "deploy_to_vagrant.sh - requires path to vagrant file as the single argument. eg deploy_to_vagrant.sh /home/ian/dev/folio-vagrant/"
  echo "deploy_to_vagrant.sh /path/to/vagrantfile"
  exit 1
fi

script_base=$(dirname "$(readlink -f "$0")")

cd $1

# OPTIONS=`vagrant ssh-config | awk -v ORS=' ' '{print "-o " $1 "=" $2}'`
vagrant ssh-config > /tmp/cfg.txt


echo $OPTIONS

# echo $script_base
# vagrant ssh -c "pwd"
# vagrant ssh -c "ls"
# vagrant ssh -c "ls /etc/folio/deployment-descriptors/"
# vagrant ssh -c "ls /etc/folio/module-descriptors/"

# scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/libs/folio-demo-module-0.1.war default:/home/vagrant
# scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/DeploymentDescriptor.json default:/etc/folio/deployment-descriptors/mod-grails-demo
# scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/ModuleDescriptor.json default:/etc/folio/module-descriptors/mod-grails-demo

# Refine - step 1 : Copy files to vagrant user
scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/libs/folio-demo-module-0.1.war default:
scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/DeploymentDescriptor.json default:
scp -F /tmp/cfg.txt $script_base/../folio-demo-module/build/ModuleDescriptor.json default:
scp -F /tmp/cfg.txt $script_base/../attic/folio_globals.yaml default:

vagrant ssh -c "sudo mv DeploymentDescriptor.json /etc/folio/deployment-descriptors/mod-grails-demo.json"
vagrant ssh -c "sudo mv ModuleDescriptor.json /etc/folio/module-descriptors/mod-grails-demo.json"
vagrant ssh -c "sudo mv folio-demo-module-0.1.war /home/folio/folio-demo-module.war"
vagrant ssh -c "sudo mv folio_globals.yaml /home/folio/folio_globals.yaml"
vagrant ssh -c "sudo chown folio:folio /etc/folio/deployment-descriptors/mod-grails-demo /etc/folio/module-descriptors/mod-grails-demo /home/folio/folio-demo-module.war /home/folio/folio_globals.yaml"
vagrant ssh -c "curl -i -w '\n' -X POST -H 'Content-type: application/json' -d @/etc/folio/module-descriptors/mod-grails-demo.json http://localhost:9130/_/proxy/modules"

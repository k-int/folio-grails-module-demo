#!/bin/sh

if [ $# -eq 0 ]
then
  echo "deploy_to_vagrant.sh - requires path to vagrant file as the single argument. eg deploy_to_vagrant.sh /home/ian/dev/folio-vagrant/"
  echo "deploy_to_vagrant.sh /path/to/vagrantfile"
  exit 1
fi

cd $1
vagrant ssh -c "pwd"
vagrant ssh -c "ls"
vagrant ssh -c "ls /etc/folio/deployment-descriptors/"
vagrant ssh -c "ls /etc/folio/module-descriptors/"

# We have to copy ../folio-demo-module/build/DeploymentDescriptor.json to /etc/folio/deployment-descriptors/mod-grails-demo
# We have to copy ../folio-demo-module/build/ModuleDescriptor.json to /etc/folio/module-descriptors/mod-grails-demo

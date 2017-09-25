#!/bin/sh

if [ $# -eq 0 ]
then
  echo "deploy_to_vagrant.sh - requires path to vagrant file as the single argument. eg deploy_to_vagrant.sh /home/ian/dev/folio-vagrant/"
  echo "deploy_to_vagrant.sh /path/to/vagrantfile"
  exit 1
fi

script_base=$(dirname "$(readlink -f "$0")")

cd $1

OPTIONS=`vagrant ssh-config | awk -v ORS=' ' '{print "-o " $1 "=" $2}'`


echo $OPTIONS

# echo $script_base
# vagrant ssh -c "pwd"
# vagrant ssh -c "ls"
# vagrant ssh -c "ls /etc/folio/deployment-descriptors/"
# vagrant ssh -c "ls /etc/folio/module-descriptors/"

scp $OPTIONS $script_base/../folio-demo-module/build/libs/folio-demo-module-0.1.war vagrant@localhost:/home/vagrant
scp $OPTIONS $script_base/../folio-demo-module/build/DeploymentDescriptor.json vagrant@localhost:/etc/folio/deployment-descriptors/mod-grails-demo
scp $OPTIONS $script_base/../folio-demo-module/build/ModuleDescriptor.json vagrant@localhost:/etc/folio/module-descriptors/mod-grails-demo

#!/bin/bash
sudo rm -R /opt/OpenElis/openelis/dist/bahmni-lab
unzip /opt/OpenElis/openelis/dist/openelis.war -d /opt/OpenElis/openelis/dist/bahmni-lab
scp -r /opt/OpenElis/openelis/dist/bahmni-lab root@192.168.0.140:/opt/bahmni-lab/bahmni-lab

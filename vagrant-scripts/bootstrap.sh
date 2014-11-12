#!/usr/bin/env bash

export DEBIAN_FRONTEND=noninteractive

apt-get autoclean
apt-get autoremove

cp --force /vagrant/vagrant-scripts/sources.list /etc/apt/sources.list
apt-get --quiet --yes --target-release wheezy-backports update
apt-get --quiet --yes --target-release wheezy-backports upgrade

# NFS Client
apt-get --quiet --yes --target-release wheezy-backports install nfs-common

# Java
apt-get --quiet --yes --target-release wheezy-backports install openjdk-7-jdk

# MySQL
apt-get --quiet --yes --target-release wheezy-backports install mysql-server

apt-get autoclean
apt-get autoremove

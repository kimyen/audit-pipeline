VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|

  config.vm.box = "chef/debian-7.6"

  config.vm.provision :shell, path: "vagrant-scripts/bootstrap.sh"

  config.vm.network :forwarded_port, host: 3306, guest: 3306 # MySQL

  config.vm.network "private_network", ip: "192.168.55.103"
  config.vm.synced_folder "~/.gradle", "/home/vagrant/.gradle", nfs: true
  config.vm.synced_folder "~/.dashboard", "/home/vagrant/.dashboard", nfs: true

  config.vm.provider "virtualbox" do |vb|
    vb.customize ["modifyvm", :id, "--cpus", 2]
    vb.customize ["modifyvm", :id, "--memory", 2048]
  end

end

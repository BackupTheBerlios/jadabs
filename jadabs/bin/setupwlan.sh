#! /bin/bash

iwconfig eth1 essid bt-hotspot mode ad-hoc
ifconfig eth1 192.168.55.8
route add -net 224.0.0.0 netmask 240.0.0.0 dev eth1
iptables -t nat -A POSTROUTING -o eth0 -j MASQUERADE
echo "1" > /proc/sys/net/ipv4/ip_forward

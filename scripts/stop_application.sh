#!/bin/bash
cd /home/ec2-user/app
if [ -f docker-compose.yml ]; then
    docker-compose down || true
fi

#!/bin/bash
sleep 30
if curl -f http://localhost:3000 && curl -f http://localhost:8080\; then
    echo "Application started successfully"
    exit 0
else
    echo "Application failed to start"
    exit 1
fi

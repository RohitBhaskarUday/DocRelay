#!/bin/bash

# DocRelay VPS Setup Script
# This script helps set up DocRelay on Fresh VPS

# Exit on error

set -e

echo "=== DocRelay VPS Setup Script ==="
echo "This script will install Java, Node.Js, Nginx, and set up DocRelay."

# Update system
echo "Updating system packages"
sudo apt update
sudo apt upgrade -y

# Install Java
echo "Installing Java"
sudo apt install -y openjdk-17-jdk

# Install Node.js
echo "Installing Node js"
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

#Install nginx
echo "Installing Nginx..."
sudo apt install -y nginx

# Install PM2
echo "Installing PM2..."
sudo npm install -g pm2

# Install Maven
echo "Installing Maven..."
sudo apt install -y maven

# Clone repository (uncomment and modify if using Git)
# echo "Cloning repository..."
#git clone https://github.com/RohitBhaskarUday/DocRelay
#cd DocRelay

# Build backend
echo "Building Java backend..."
mvn clean package

# Build frontend
echo "Building frontend..."
cd ui
npm install
npm run build
cd ..

# Set up Nginx
echo "Setting up Nginx..."

# Ensure the default site is removed to avoid conflicts
if [ -e /etc/nginx/sites-enabled/default ]; then
    sudo rm /etc/nginx/sites-enabled/default
    echo "Removed default Nginx site configuration."
fi

# Create the peerlink configuration file with the correct content
echo "Creating /etc/nginx/sites-available/docrelay..."
cat <<EOF | sudo tee /etc/nginx/sites-available/docrelay
server {
    listen 80;
    server_name _; # Catch-all for HTTP requests

    # Backend API
    location /api/ {
        proxy_pass http://localhost:9000/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }

    # Frontend
    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_cache_bypass \$http_upgrade;
    }

    # Additional security headers (still good to have)
    add_header X-Content-Type-Options nosniff;
    add_header X-Frame-Options SAMEORIGIN;
    add_header X-XSS-Protection "1; mode=block";
}
EOF

# Create the symbolic link to enable the docrelay site
sudo ln -sf /etc/nginx/sites-available/docrelay /etc/nginx/sites-enabled/docrelay

sudo nginx -t
if [ $? -eq 0 ]; then
    sudo systemctl restart nginx
    echo "Nginx configured and restarted successfully."
else
    echo "Nginx configuration test failed. Please check /etc/nginx/nginx.conf and /etc/nginx/sites-available/docrelay."
    exit 1
fi

# Set up SSL with Let's Encrypt (uncomment if needed)
# echo "Setting up SSL with Let's Encrypt..."
# sudo apt install -y certbot python3-certbot-nginx
# sudo certbot --nginx -d your-actual-domain.com

# Start backend with PM2
echo "Starting backend with PM2..."
# Ensure all dependencies are in the classpath
CLASSPATH="target/docrelay-1.0-SNAPSHOT.jar:$(mvn dependency:build-classpath -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout -q)"
pm2 start --name docrelay-backend java -- -cp "$CLASSPATH" docrelay.App

# Start frontend with PM2
echo "Starting frontend with PM2..."
cd ui
pm2 start npm --name docrelay-frontend -- start
cd ..

# Save PM2 configuration
pm2 save

# Set up PM2 to start on boot
echo "Setting up PM2 to start on boot..."
pm2 startup
# Follow the instructions printed by the above command

echo "=== Setup Complete ==="
echo "PeerLink is now running on your VPS!"
echo "Backend API: http://localhost:9000 (Internal - accessed via Nginx)"
echo "Frontend: http://your_lightsail_public_ip (Access via your instance's IP address)"
echo "You can access your application using your Lightsail instance's public IP address in your browser."
# echo "Visit https://your-actual-domain.com to access your application."
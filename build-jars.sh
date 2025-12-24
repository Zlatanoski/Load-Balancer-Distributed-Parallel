#!/bin/bash

echo "Building JARs manually..."

# Navigate to project root
cd ~/Desktop/LoadBalancing-Paralle

# Clean and create directories
rm -rf out/manual
mkdir -p out/manual/WORKER
mkdir -p out/manual/PROXY

# Check if compiled classes exist
if [ ! -d "out/production/LoadBalancing-Paralle" ]; then
    echo "ERROR: No compiled classes found!"
    echo "Please build the project in IntelliJ first (Build -> Build Project)"
    exit 1
fi

# Worker JAR
echo "Creating Worker JAR..."
cd out/production/LoadBalancing-Paralle
mkdir -p temp_worker/META-INF
echo -e "Manifest-Version: 1.0\nMain-Class: WorkerMain\n" > temp_worker/META-INF/MANIFEST.MF
cp *.class temp_worker/ 2>/dev/null
cd temp_worker
zip -q -r ~/Desktop/LoadBalancing-Paralle/out/manual/WORKER/worker.jar .
cd ~/Desktop/LoadBalancing-Paralle/out/production/LoadBalancing-Paralle
rm -rf temp_worker

# Proxy JAR
echo "Creating Proxy JAR..."
mkdir -p temp_proxy/META-INF
echo -e "Manifest-Version: 1.0\nMain-Class: ProxyMain\n" > temp_proxy/META-INF/MANIFEST.MF
cp *.class temp_proxy/ 2>/dev/null
cd temp_proxy
zip -q -r ~/Desktop/LoadBalancing-Paralle/out/manual/PROXY/proxy.jar .
cd ~/Desktop/LoadBalancing-Paralle/out/production/LoadBalancing-Paralle
rm -rf temp_proxy

cd ~/Desktop/LoadBalancing-Paralle

echo ""
echo "Done! Verifying..."
echo ""
echo "=== Worker JAR ==="
unzip -p out/manual/WORKER/worker.jar META-INF/MANIFEST.MF
echo ""
echo "=== Proxy JAR ==="
unzip -p out/manual/PROXY/proxy.jar META-INF/MANIFEST.MF
echo ""
echo "=== File Sizes ==="
ls -lh out/manual/WORKER/worker.jar out/manual/PROXY/proxy.jar
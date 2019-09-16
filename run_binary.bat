set LD_LIBRARY_PATH=/media/sf_wadehundley/work/source/engage/Trunk/deliverability/dist
export LD_LIBRARY_PATH


/var/jdk1.8.0_221/bin/java -Djava.library.path=/media/sf_wadehundley/work/source/engage/Trunk/deliverability/dist -jar out/artifacts/deliverability_jar/deliverability.jar "data/acct.40" "data/" 10000

#!/bin/bash

#jorge@192.92.149.171
#jmgomes@ui-hpc.ncg.ingrid.pt

ip=$1
shift

rsync -vur dist/lib $ip:
rsync -vur build $ip:
rsync -vu gridrun.sh $ip:

for f in "$@"
do
	rsync -vur $f $ip:
done


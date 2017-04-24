#!/bin/bash

rsync -vur dist/lib jorge@192.92.149.171:
rsync -vur build jorge@192.92.149.171:
rsync -vu hpcrun.sh jorge@192.92.149.171:

for f in "$@"
do
	rsync -vur $f jorge@192.92.149.171:
done

#rsync -vur dist/lib jmgomes@ui-hpc.ncg.ingrid.pt:
#rsync -vur build jmgomes@ui-hpc.ncg.ingrid.pt:
#rsync -vu hpcrun.sh jmgomes@ui-hpc.ncg.ingrid.pt:
#
#for f in "$@"
#do
#	rsync -vur $f jmgomes@ui-hpc.ncg.ingrid.pt:
#done

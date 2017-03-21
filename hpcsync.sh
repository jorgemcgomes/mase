#!/bin/bash
rsync -vur dist/lib jmgomes@ui-hpc.ncg.ingrid.pt:
rsync -vur build jmgomes@ui-hpc.ncg.ingrid.pt:
rsync -vu hpcrun.sh jmgomes@ui-hpc.ncg.ingrid.pt:

for f in "$@"
do
	rsync -vur $f jmgomes@ui-hpc.ncg.ingrid.pt:
done

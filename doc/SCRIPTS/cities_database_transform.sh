#!/bin/bash
#
# brew install gnu-sed
# brew install coreutils
#
export LC_ALL='C'
dataFileCompressed=Gaz_places_national.txt.gz
dataFile=Gaz_places_national.txt
targetFile=cityall.txt

gzip -d ${dataFileCompressed}
dos2unix ${dataFile}
awk -F'\t' '{print $4","$13","$14","$7}' ${dataFile} | gsed 's/ *,/,/g' | gsed 's/ city,/,/' \
        | gsed 's/ CDP,/,/' | gsed 's/ borough,/,/' | gsed 's/ town,/,/' | gsed 's/ village,/,/' \
       | gsed 's/ comunidad,/,/' | gsed 's/ urbana,/,/'  | gsed 's/ zona,/,/'  | gsort -r -n -t, -k4 > ${targetFile}

gzip ${dataFile}

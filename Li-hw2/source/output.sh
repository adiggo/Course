mkdir -p outputs


for i in {1..20}
do
    for j in {18..22}
    do
	for k in 1 2 4 8 16
	do
	    java ParallelDES $k $j > ./outputs/output_${j}_${k}_${i}.txt
	done
    done
done




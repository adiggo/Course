#for i in {1..20}
#do
#./filter > ./outputs/serial_result$i.txt
#done

for i in {1..20}
do
./filterp1 > ./outputs/parallel_result$i.txt
done


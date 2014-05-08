# code for question 1
result = matrix(0,nrow=11,ncol = 3);
nsample = 20;
for(i in 1:nsample){
data = read.table(paste("./outputs/serial_result",i,".txt",sep=''),header=FALSE);
dataFirst = as.matrix(data[1+2*(0:10),c(5,8,13)]);
tmp = dataFirst[,1] + dataFirst[,2]/1000000 ;
dataFirst = cbind(dataFirst,tmp);
filterFirst = as.matrix(data[2*(1:11),c(5,8,13)]);
tmp = filterFirst[,1] + filterFirst[,2]/1000000;
filterFirst = cbind(filterFirst,tmp);
# columns of data: dataFirst time, filterFirst time, filter len
data = cbind(dataFirst[,4],filterFirst[,4:3]);
result[,1:2]=result[,1:2]+data[,1:2];
}
result[,3] = data[,3];
result[,1:2] = result[,1:2]/nsample;
# result is the average of 20 outputs, column contents same as data

norm_data = result;
tmp1 = 512*512*128*norm_data[,3] / norm_data[,1]
tmp2 = 512*512*128*norm_data[,3] / norm_data[,2]
norm_data[,1] = norm_data[,1] / norm_data[,3]
norm_data[,2] = norm_data[,2] / norm_data[,3]
norm_data = cbind(norm_data,tmp1,tmp2)
# columb of norm_data: dataFirst time / filter len, filterFirst time / filter len, filter len
# ,dataFirst operations per sec, filterFirst operations per sec


png("1_seriel_run_time_vs_filter_len.png")
matplot(data[,3],data[,1:2],xlab="Filter Length",ylab="Time(sec)",main="Running Time vs Filter Length",type="l",col=c("black","green"))
legend(100,80,col=c("black","green"),legend=c("dataFirst","filterFirst"),lty=c(1,2))
dev.off()

png("2_serial_operation_per_sec_vs_filter_len.png")
matplot(norm_data[,3],norm_data[,4:5],xlab="Filter Length",ylab="Operations/sec",main="Operations/sec vs Filter Length", type="l",col=c("black","green"))
legend(400,4.7e+08,col=c("black","green"),legend=c("dataFirst","filterFirst"),lty=1:2)
dev.off()

png("3_serial_relative_performance_vs_filter_len.png")
plot(data[,3],data[,1]/data[,2],xlab="Filter Length",ylab="dataFirst time / filterFirst time",main="Relative Performance vs Filter Length", type="l",col="blue" )
dev.off()

# code for question 2
dataFirst = rep(0,5)
filterFirst = rep(0,5)
for(i in 1:20){
	data = read.table(paste("./outputs/parallel_result",i,".txt",sep=''));
	data = data[2:11,5] + data[2:11,8]/1000000;
	filterFirst = filterFirst + data[c(1,3,5,7,9)];
	dataFirst = dataFirst + data[c(2,4,6,8,10)];
}
filterFirst = filterFirst / 20;
dataFirst = dataFirst / 20;
data = cbind(dataFirst,filterFirst)
png("4_parallel_run_time_vs_nthreads_fl512.png")
matplot(0:4,data,xlab="log(#threads)",ylab="Time(sec)",main="Running Time vs # Threads (filter len = 512)", type="l",col=c("black","green"))
legend(3,35,col=c("black","green"),legend=c("dataFirst","filterFirst"),lty=c(1,2))
dev.off()

data[,1]=data[,1]/data[1,1]
data[,2]=data[,2]/data[1,2]
ydf = data[,1] - 1
yff = data[,2] - 1
x= (1/(c(1,2,4,8,16))-1)
lm.fit1 = lm(ydf~x-1)
lm.fit2 = lm(yff~x-1)
lm.fit1
lm.fit2

# code for question 2.2
zdf=matrix(0,ncol=11,nrow=5)
zff=matrix(0,ncol=11,nrow=5)
for(i in 0:10){
	files = dir(paste("./outputs/",2^i,"/",sep=''),full.name=TRUE)
	for(j in 1:20){
		data=read.table(files[j]);
		data=data[2:11,5] + data[2:11,8]/1000000;
		zdf[,i+1] = zdf[,i+1] + data[2*(1:5)];
		zff[,i+1] = zff[,i+1] + data[2*(1:5)-1];
	}
	zdf[,i+1] = zdf[,i+1]/20;
	zff[,i+1] = zff[,i+1]/20;
}

x=1:5
y=0:10
library(rgl)
persp3d(x,y,zdf,color="lightblue",xlab="#threads",ylab="log(filter len)",zlab="time")
rgl.snapshot("3d-plot-df.png")
persp3d(x,y,zff,color="lightblue",xlab="#threads",ylab="log(filter len)",zlab="time")
rgl.snapshot("3d-plot-ff.png")
diag = diag(zdf)
pdf("2-2.pdf")
plot(diag,type="l",xlab="#threads/log2(filter len)",ylab="time",main="Running time increasing filter len & #threads simultaneously")
dev.off()

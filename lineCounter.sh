line()
{
n=0
dir=$1
for file in `ls $dir`
do
    if [ -d $dir'/'$file ]
        then
            l=`line $dir'/'$file`
            let n=$n+$l
    else
        l=`wc -l $dir'/'$file`
        l=`echo $l | cut -d ' ' -f1`
        let n=$n+$l
    fi
done
echo $n
}

if [ $# -lt 1 ]
then 
echo 'Usage: ./lineCounter directory'
exit
fi
a=`line $1`
echo $a

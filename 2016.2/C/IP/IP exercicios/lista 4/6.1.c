#include<stdio.h>
#include<stdlib.h>

main()
{
    //variaveis
    int i,j,v,w=9;
    int m[10][10];


    for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
                m[i][j]=0;
        }

    }
     for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
            if(j!=w)
            printf("%i\t",m[i][j]);
            else if(j==w)
            {
                printf("\t");
            }
        }
        w--;
        printf("\n");

    }
}

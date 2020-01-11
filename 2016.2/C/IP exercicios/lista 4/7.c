#include<stdio.h>
#include<stdlib.h>

main()
{
    int i,j,w;
    int m[6][6];

    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            m[i][j]=1;

        }
    }
    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            printf("%i",m[i][j]);

        }
        printf("\n");
    }
    printf("\n\n");
    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            w=i+j;
            if(w%2==0)
            printf("%i",m[i][j]);
            else
            {
                printf(" ");
            }

        }
        printf("\n");
    }

}

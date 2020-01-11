#include <stdio.h>
#include<stdlib.h>

main()
{
    int A[5][5], B[5][5], SOMA[5][5];
    int i,j;

    for(i=0;i<5;i++)
    {
        for(j=0;j<5;j++)
        {
            A[i][j]=1;
            printf("%i", A[i][j]);
        }
        printf("\n");
    }
    printf("\n");
    printf("\+");
    printf("\n");
    for(i=0;i<5;i++)
    {
        for(j=0;j<5;j++)
        {
            B[i][j]=0;
            printf("%i", B[i][j]);

        }
        printf("\n");

    }
}

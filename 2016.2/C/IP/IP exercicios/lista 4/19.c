#include <stdio.h>
#include<stdlib.h>

main()
{
    int A[3][3], B[3][3], Produto[3][3];
    int i,j;

    for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
            A[i][j]=i;
            printf("%i", A[i][j]);
        }
        printf("\n");
    }
    printf("\n");
    printf("\X");
    printf("\n");
    for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
            B[i][j]=i;
            printf("%i", B[i][j]);

        }
        printf("\n");

    }
     printf("\n");
    printf("\=");
    printf("\n");
     for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
			Produto[i][j]= A[i][j]*B[j][i];            
			printf("%i", Produto[i][j]);

        }
        printf("\n");

    }
}

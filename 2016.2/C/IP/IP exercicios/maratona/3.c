#include<stdio.h>
#include<stdlib.h>

main()
{
    int i,c,j;
    int v[4];
    int tamanho=4;

    for(i=0;i<3;i++)
    {
        printf("Digite v[%i]: ", i);
        scanf("%i", &v[i]);
    }
    for(i=0;i<3;i++)
    {
        printf("%i ", v[i]);
    }
    printf("\n");
    for(i=3;i>=1; i--)
    {
        for(j=0;j<2;i++)
        {
            if(v[i]>v[j+1])
            {
                c=v[j];
                v[j]=v[j+1];
                v[j+1]=c;
            }
        }
    }
    for(i=0;i<4;i++)
    {
        printf("%i ", v[i]);
    }
}

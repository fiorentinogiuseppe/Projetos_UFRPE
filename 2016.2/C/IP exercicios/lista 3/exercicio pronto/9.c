#include<stdio.h>
#include<stdlib.h>
#include<math.h>

int main()
{
    int v1,v2,i,v1_2,v2_2,v3;

    printf("Digite os 2 termos iniciais da serie de FETUCCINE: ");
    scanf("%i %i", &v1, &v2);

        v1_2=v1;
        v2_2=v2;
        printf("%i\n%i\n", v1_2, v2_2);
    for(i=3; i<=10;i++)
    {

        if(i%2!=0)
        {
            v3=v1_2+v2_2;
            v1_2= v2_2;
            v2_2=v3;
            printf("%i\n", v3);
        }
        else if(i%2==0)
        {
            v3=v2_2-v1_2;
            v1_2= v2_2;
            v2_2=v3;
            printf("%i\n", v3);

        }
    }
}

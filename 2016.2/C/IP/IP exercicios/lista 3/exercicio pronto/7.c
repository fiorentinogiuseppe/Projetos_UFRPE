#include<stdio.h>
#include<stdlib.h>

int main()
{
    int n,i,val;
    printf("Digite o numero N de entradas: ");
    scanf("%i",&n);

    system("cls");

    for(i=1; i<=n; i++)
    {
        printf("Digite o %i.o de N: ", i);
        scanf("%i",&val);

        printf("O seu triplo eh: %i\n\n", val*3);

        system("pause");
        system("cls");
    }
}

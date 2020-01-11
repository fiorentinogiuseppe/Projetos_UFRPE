#include<stdio.h>
#include<stdlib.h>
#include<math.h>

int main()
{
    int i=0;
    int idade;
    char s;
    int contador=0;

    while(i<20)
    {

        i++;
        printf("Digite sua idade: ");
        scanf("%i", &idade);
        getchar();
        printf("Digite seu sexo: [m/f]: ");
        scanf("%c", &s);

        if(s=='m' && idade>=21)
        {
            contador++;
            system("cls");
            printf("Pessoa %i tem mais de 21 anos e eh masculino.\n\n\n", contador);
        }

        system("pause");
        system("cls");

    }
}

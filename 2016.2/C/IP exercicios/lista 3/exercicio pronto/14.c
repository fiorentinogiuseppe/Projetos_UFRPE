#include<stdio.h>
#include<stdlib.h>


int main()
{
    int resp;

    do
    {
    printf("BOAS VINDAS\n");
    printf("1- Ola\n");
    printf("2- Bem vindo\n");
    printf("3- Sair do algoritimo\n");
    scanf("%i", &resp);

    switch (resp)
    {
    case 1:
    {
        printf("Ola\n");
        break;
    }
    case 2:
    {
        printf("Bemvindo\n");
        break;
    }
    case 3:
    {
        printf("Saindo\n");
        break;
    }
    default:
    {
    printf("valor errado\n");
    }

    }
    system("pause");
    system("cls");
    }
    while(resp!=3);
}

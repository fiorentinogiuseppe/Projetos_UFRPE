#include<stdio.h>
#include<stdlib.h>

main()
{
    int idade, contador_de_pessoas=0,i=0;
    char sexo;

    do
    {
        i++;

        printf("Digite sua idade: ");
        scanf("%i", &idade);
        getchar();
        printf("Escolha seu sexo: [M/F] ");
        scanf("%c", &sexo);

        if(sexo== 'm')
        {

                contador_de_pessoas++;


        }
         system("pause");
        system("cls");
    }
    while(i<3);
    printf("%i", contador_de_pessoas);


}


#include<stdio.h>
#include<stdlib.h>
#include<string.h>
main()
{
    char valor,soma;
    printf("X:");
    scanf("%c",&valor);
    soma=valor + 'A' - 'a';
    printf("\n\n\na soma eh  %c",soma);
}

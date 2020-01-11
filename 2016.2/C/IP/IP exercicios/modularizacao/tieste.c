#include<stdio.h>
#include<stdlib.h>

int fatorial(int x)
{
    int i;
    int fat=1;
    for(i=x;i>=1;i--)
    {
        fat=fat*i;
    }
    return fat;
}



main()
{
    int numero;
    printf("Digite um numero: ");
    scanf("%i",&numero);

    fatorial(numero);
    printf("O fatorial de %i eh: %i", numero, fatorial(numero));
}

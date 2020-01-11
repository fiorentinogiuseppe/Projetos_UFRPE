#include<stdio.h>
#include<stdlib.h>

main()
{
    int maior=0;
    int i, numeros, valor;
    printf("Quantos numeros voce deseja digitar?: ");
    scanf("%i",&numeros);

    i=0;
    while(i<numeros);
    {
        i++;
        printf("Digite o valor:");
        scanf("%i", valor);
        if(valor>maior)
        {
            maior=valor;
        }
    }
    printf("%i",maior);

}


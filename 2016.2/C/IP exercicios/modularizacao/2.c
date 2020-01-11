#include<stdio.h>
#include<stdlib.h>

int fatoracao(int x,int y)
{
    int contador=0;
    do{

        if(x%y==2 && x/y>1)
        {
            contador++;
        }
    }while(x/y!=1);
    return contador;

}



main()
{
    int numero1,numero2;
    printf("Digite um numero: ");
    scanf("%i",&numero1);
    printf("Digite outro numero: ");
    scanf("%i",&numero2);

    fatoracao(numero1,numero2);
    if(fatoracao(numero1,numero2)==0)
    {
        printf("Nao eh divisivel");
    }
    else
    printf("O numero %i eh divisivel por %i %i vezes ",numero1,numero2,fatoracao(numero1,numero2) );
}

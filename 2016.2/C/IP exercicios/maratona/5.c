#include<stdio.h>
#include<stdlib.h>

main()
{
    int n,i,a,b,c;
    do
    {
        printf("Digite o valor de n>0: ");
        scanf("%i",&n);
        if(n>0)
        {
          //fibonacci
          int a=0, b=1;
            for(i=3;i<=n;i++)
            {
                c=a+b;
                a=b;b=c;
                if(i==n)
                {

                    printf("%i",c);
                }
            }

        }
        else
        {
            printf("Digite um valor maior que '0'\n");
            system("pause");
            system("cls");
        }

    }
    while(n<=0);




}

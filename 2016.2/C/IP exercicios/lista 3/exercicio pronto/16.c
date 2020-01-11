#include<stdio.h>
#include<stdlib.h>

main()
{
        int resp, classe, itens, hora,extra, salario_final, vendas;
        float fim;
        char resp_2;

        do
        {
            printf("---------------------------------");
            printf("\n      Bem Vindo a empresa JJJ\n");
            printf("---------------------------------");
            printf("\nEscolha o que voce deseja fazer: ");
            printf("\n[1] calcular seu salario.\n");
            printf("[2] sair.\n");
            printf("---------------------------------\n");
            scanf("%i", &resp);
            do
            {
              if(resp==1)
                {
                    system("cls");
                    printf("----------------------------------");
                    printf("\nEscolha a sua classe de trabalho: ");
                    printf("\n[1] Gerente.");
                    printf("\n[2] Horista.");
                    printf("\n[3] Trabalhador de comissao.");
                    printf("\n[4] Tarefeiro.");
                    printf("\n[5] Sair.\n");
                    printf("---------------------------------\n");
                    scanf("%i", &classe);

                    switch(classe)
                    {
                        case 1:
                            system("cls");
                            printf("Seu salario eh: R$ 2000.00\n\n\n");
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");
                        break;
                        case 2:
                            system("cls");
                            printf("Digite a quantidade de de horas trabalhadas por voce: ");
                            scanf("%i", &hora);
                            system("pause");
                            system("cls");
                            if(hora<=40)
                            {
                                printf("Seu salario eh de: R$800.00\n\n\n");
                            }
                            else
                            {
                                extra= hora-40;
                                salario_final= (extra*(1.5*800)+800);
                                printf("Seu salario eh de: R$%i.00\n\n\n", salario_final);
                            }
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");

                        break;
                        case 3:
                            system("cls");
                            printf("Digite a quantidade de vendas que voce realisou: ");
                            scanf("%i", &vendas);
                            fim= (((vendas*(610*5.7))/100)+610);
                            system("pause");
                            system("cls");
                            printf("Seu salario eh de: R$%.2f\n\n\n", fim);
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");


                        break;
                        case 4:
                            system("cls");
                            printf("Digite a quantidade de itens produzidos por voce: ");
                            scanf("%i", &itens);
                            system("pause");
                            system("cls");
                            printf("Seu salario eh de: R$%i.00\n\n\n",itens*1000);
                            system("pause");
                            system("cls");
                            getchar();
                            printf("Deseja fazer uma nova consulta?: [S/N] ");
                            scanf("%c", &resp_2);
                            system("pause");
                            system("cls");
                        break;
                        case 5:
                        	exit(0);
                        	break;
                       
                        
                    }
                }
                else if( resp==2)
                {
                	exit(0);
				}
			

            }
            while(resp_2!='n');


        }
        while(resp!=2);
}

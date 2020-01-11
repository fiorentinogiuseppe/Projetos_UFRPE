#include<stdio.h>
#include<stdlib.h>

main()
{
	int mes[12];
	char resp,resultado;
	int custo=0,big,less,i,j, MES;
	do
	{
		printf("#####################################################\n");
		printf("Diga o mes que voce quer guardar o custo: \n");
		printf("[1] Janeiro;\n");
		printf("[2] Fevereiro;\n");
		printf("[3] Marco;\n");
		printf("[4] Abril;\n");
		printf("[5] Maio;\n");
		printf("[6] Junho;\n");
		printf("[7] Julho;\n");
		printf("[8] Agosto;\n");
		printf("[9] Setembro;\n");
		printf("[10] Outubro;\n");
		printf("[11] Novembro;\n");
		printf("[12] Dezembro.\n");
		printf("#####################################################\n");
		scanf("%i", &MES);
		
		switch (MES)
		{
			case 1:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[1]=custo;
				break;
			}
			case 2:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[2]=custo;
				break;
			}
			case 3:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[3]=custo;
				break;
			}
			case 4:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[4]=custo;
				break;
			}
			case 5:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[5]=custo;
				break;
			}
			case 6:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[6]=custo;
				break;
			}
			case 7:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[7]=custo;
				break;
			}
			case 8:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[8]=custo;
				break;
			}
			case 9:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[9]=custo;
				break;
			}
			case 10:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[10]=custo;
				break;
			}
			case 11:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[11]=custo;
				break;
			}
			case 12:
			{
				system("cls");
				printf("Digite o custo do mes: ");
				scanf("%i", &custo);
				mes[12]=custo;
				break;
				
			}
			default:
			{
				system("cls");
				printf("Mes invalido!!\n\n\n\n\n");	
				break;
			}
			
		}
		
			less=mes[1];
			big=mes[1];
			
		for(i=0;i<12;i++)
		{
			if(mes[i]>big)
			{
				big=mes[i];
			}
			else if(mes[i]<less)
			{
				less=mes[i];
			}
		}
		
		system("pause");
		system("cls");
		getchar();
		
		printf("Voce deseja salvar outro mes?: [S/N]");
		scanf("%c", &resp);
		system("pause");
		system("cls");
		
		if(resp=='n')
		{
			system("cls");
			getchar();
			printf("Deseja mostrar os dados dos meses?[S/N]: ");
			scanf("%c", &resultado);
			
			if(resultado=='s')
			{
				for(i=0;i<12;i++)
				{
					printf("mes %i: %i\n",i+1, mes[i]);
				}
				printf("O mes de maior apuracao foi: %i\n", big);
				printf("O mes de menor apuracao foi: %i\n", less);
				
				
			}
		
		}
	}while (resp!='n');
	
}

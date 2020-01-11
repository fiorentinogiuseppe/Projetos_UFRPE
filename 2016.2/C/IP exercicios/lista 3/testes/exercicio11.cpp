#include<stdio.h>
#include<stdlib.h>

main()
{	
	char resp;
	
	
	do
	{
		int tamanho;
	
		printf("Digite o tamanho n da matriz; 2<=n<=6: ");
		scanf("%i", &tamanho);
			if(tamanho<2 || tamanho>6)
			{
				printf("Digite valores da forma que foi especificada!\a\a\n\n\n");
			}
			else
			{
				//matriz A
				int i,j;
				int m[tamanho][tamanho];
				for(i=0;i<tamanho;i++)
				{
					for(j=0; j<tamanho; j++)
					{
						m[i][j]= -5 +(rand()%5);
						printf("%i\t", m[i][j]);
					}
					printf("\n");
				}
				printf("X");
				printf("\n");
				
				//matriz B
				int i_2,j_2;
				int m_2[tamanho][tamanho];
				for(i_2=0;i_2<tamanho;i_2++)
				{
					for(j_2=0; j_2<tamanho; j_2++)
					{
						m_2[i_2][j_2]= -5 +(rand()%5);
						printf("%i\t", m_2[i_2][j_2]);
					}
					printf("\n");
				}
				printf("=");
				printf("\n");
				
				//matriz produto
				
				int i_3,j_3;
				int m_3[tamanho][tamanho];
				for(i_3=0;i_3<tamanho;i_3++)
				{
					for(j_3=0; j_3<tamanho; j_3++)
					{
						m_3[i_3][j_3]= m[i_3][j_3]*m_2[j_3][i_3];
						printf("%i\t", m_3[i_3][j_3]);
					}
					printf("\n");
				}
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
				
			}
			
			
			
			
			
			
			
			
			
		printf("Deseja sair?[s/n] ");
		getchar();
		scanf("%c", &resp);
		system("cls");
	}
	while(resp!='s');

	
}

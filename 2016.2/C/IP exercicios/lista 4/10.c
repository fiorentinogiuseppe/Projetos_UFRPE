#include<stdio.h>
#include<stdlib.h>

main()
{
	int inicial[3][3];
	int custo[3][3];
	int i,j;
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<3;j++)
		{
			printf("Digite o estoque inicial: ");
			scanf("%i", &inicial[i][j]);
		}
	
	}
	for(i=0;i<3;i++)
	{
		printf("\tproduto %i", i+1);
	}
	printf("\n");
		for(i=0;i<3;i++)
	{
		printf("Armazem %i\t", i+1);
		for(j=0;j<3;j++)
		{
			printf("%i\t\t", inicial[i][j]);
		}
		printf("\n");
	}
	int big, armazem=1;
	for(i=0;i<3;i++)
	{
		for(j=0;j<3;j++)
		{
			if(j==2)
			{
				if(inicial[i][j]>big)
				{
					armazem++;
					big=inicial[i][2];
				}	
			}
				
		}
	}
	
	printf("O armazem %i tem a maior quantidade de produtos que eh %i", armazem, big);
	
}

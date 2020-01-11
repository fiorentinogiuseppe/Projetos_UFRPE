#include<stdio.h>
#include<stdlib.h>

main()
{
	int valores[6][6];
	int i,j,w,vida=6, pontos=0,a,b,k;
	int resposta_linha[6], resposta_coluna[6];
	int resposta[6][6];
	
	//recebimento de dados da tabela
	for(i=0;i<6;i++)
	{
		for(j=0;j<6;j++)
		{
			w=0;
			if(j<=3)
				valores[i][j]=w;
			if(j>3)
				valores[i][j]= --w;
		}
		w++;
	}
	
	//tabela inicial
	
	for(i=0;i<6;i++)
	{
		
		printf("\t%i",i);
	}
	printf("\n\n");
	for(i=0;i<6;i++)
	{
		printf("%i\t",i);
		for(j=0;j<6;j++)
		{
			printf("%i\t", valores[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	
	//tabela limpa
	
	for(i=0;i<6;i++)
	{
		
		printf("\t%i",i);
	}
	printf("\n\n");
	for(i=0;i<6;i++)
	{
		printf("%i\t",i);
		for(j=0;j<6;j++)
		{
			printf("\t");
		}
		printf("\n");
	}
	
	
	// recebimento dos dados da pessoa
	do
	{
		//Dados
		printf("Pontos: %i\n",pontos);
		printf("Vidas: %i\n", vida);
		
		// recebimento
		for(i=0;i<2;i++)
		{
			printf("--------------------------------------------\n");
			printf("Digite o valor da linha de v%i:",i+1 );
			scanf("%i", &resposta_linha[i]);
			printf("Digite o valor da coluna de v%i:",i+1 );
			scanf("%i", &resposta_coluna[i]);	
		}
		
		system("pause");
		system("cls");
		
		//tabela preenchida
				
		
		//pontuacao e vida
		
		
	}
	while(vida!=0);
	
	//Vencedor
	if(pontos==18)
	{
		system("cls");
		printf("PARABENS!!! VOCE VENCEU!!!");
	}
	else if(vida==0)
	{
		system("cls");
		printf("Voce perdeu!!! Tente novamente");
	}
	
	
}

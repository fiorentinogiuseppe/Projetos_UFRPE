#include<stdio.h>
#include<stdlib.h>
#include<time.h>
main()
{
	int dado1_pessoa1,dado2_pessoa1,dado1_pessoa2,dado2_pessoa2, jogador1,jogador2,pessoa1,pessoa2,resp,rodada=0,vida1=10,vida2=10,vencedor;
	int tabuleiro[7][10];
	int i,j;
	srand(time(NULL));
	do
	{
		//tabela
		
		
		
		for(i=0;i<7;i++)
		{
			for(j=0;j<10;j++)
			{
				tabuleiro[i][j]= '*';
			}
		}
		
		for(i=0;i<7;i++)
		{
			for(j=0;j<10;j++)
			{
				if(i==0)
				{
					tabuleiro[i][j]=j;
				}
				else if(i==6)
				{
					tabuleiro[i][j]=j;
				}
				else if(j==9)
				{
					if(i!=0)
					tabuleiro[i][j]=j;
				}
				else if(j==0)
				{
					if(i>=3)
					tabuleiro[i][j]=j;
				}
			}
		}
		
		for(i=0;i<7;i++)
		{
			for(j=0;j<10;j++)
			{
				if(i==0)
				{
					if(j==0)
					{//inicio
						tabuleiro[i][j]=0;
					}
					if(j==1 || j==4 || j==6 || j==8)
					{ 	//branco
						tabuleiro[i][j]=1;
					}
					if(j==2 || j==9)
					{//vermelho
						tabuleiro[i][j]=2;
					}
					if(j==3)
					{ 	//verde
						tabuleiro[i][j]=3;
					}
					if(j==7)
					{//amarelo
						tabuleiro[i][j]=4;
					}
					if(j==5)
					{//azu
						tabuleiro[i][j]=5;
					}	
				}
				if(i==6)
				{
					if(j==7 || j==5 || j==2)
					{ 	//branco
						tabuleiro[i][j]=1;
					}
					if(j==4 || j==0 || j==9)
					{//vermelho
						tabuleiro[i][j]=2;
					}
					if(j==8 || j==3)
					{ 	//verde
						tabuleiro[i][j]=3;
					}
					if(j==6)
					{//amarelo
						tabuleiro[i][j]=4;
					}
					if(j==1)
					{//amarelo
						tabuleiro[i][j]=5;
					}
				}
				
				if(j==9)
				{
					if(i==4 || i==5)
					{ 	//branco
						tabuleiro[i][j]=1;
					}
					if(i==1)
					{ 	//verde
						tabuleiro[i][j]=3;
					}
					if(i==3)
					{//azu
						tabuleiro[i][j]=5;
					}	
					if(i==2)
					{//azu
						tabuleiro[i][j]=6;
					}
				}
				if(j==0)
				{
					if(i==3)
					{//fim
						tabuleiro[i][j]=10;
					}
					if(i==4)
					{ 	//verde
						tabuleiro[i][j]=3;
					}
					if(i==5)
					{//amarelo
						tabuleiro[i][j]=4;
					}
				}
			}
		}
		for(i=0;i<7;i++)
		{
			for(j=0;j<10;j++)
			{
				printf("%i\t",tabuleiro[i][j]);
			}
			printf("\n");
		}
		
		
		//ate aq okay;
		
		
		//primeiro jogador
		int j1_1,j1_2,j2_1,j2_2,result,dadou;
		
			do
			{
				
				//pessoa2
				dado1_pessoa2=1+rand()%6;
				dado2_pessoa2=1+rand()%6;
				pessoa2=dado1_pessoa2+dado2_pessoa2;
				//pessoa1
				dado1_pessoa1=1+rand()%6;
				dado2_pessoa1=1+rand()%6;
				pessoa1=dado1_pessoa1+dado2_pessoa1;
				
				if(pessoa1>pessoa2)
				{
					resp=1;
					result=1;
				}
				else if(pessoa2>pessoa1)
				{
					resp=2;
					result=1;
				}
				else if(pessoa1==pessoa2)
				{
					result=0;
				}
			}while(result!=1);
			
		
		if(resp==1)
		{
			//pessoa1
			dadou=1+rand()%6;
			if(rodada<=9)
			{
				tabuleiro[0][dadou]=11;
			}
			else if(rodada>9 && rodada<=15)
			{
				tabuleiro[dadou][9]=11;
			}
			else if(rodada>15 && rodada<=24)
			{
				tabuleiro[6][dadou]=11;
			}
			else if(rodada>24)
			{
				tabuleiro[dadou][0]=11;
			}
			
			//ate aq okay;
			
			
			for(i=0;i<7;i++)
			{
				for(j=0;j<10;j++)
				{
					if(tabuleiro[i][j]!='*')
					{
						if(tabuleiro[i][j]==2)
						{
							for(i=0;i<3;i++)
							{ 
								vida1--;
							}
						}
						
						if(tabuleiro[i][j]==3)
						{
							vida1++;
						}
						
						if(tabuleiro[i][j]==4)
						{
							tabuleiro[i][j]='*';
						}
						if(tabuleiro[i][j]==5)
						{
							dadou=1+rand()%6;
								if(rodada<=9)
								{
									tabuleiro[0][dadou]=11;
								}
								else if(rodada>9 && rodada<=15)
								{
									tabuleiro[dadou][9]=11;
								}
								else if(rodada>15 && rodada<=24)
								{
									tabuleiro[6][dadou]=11;
								}
								else if(rodada>24)
								{
									tabuleiro[dadou][0]=11;
								}
						}
						if(tabuleiro[i][j]==6)
						{
							tabuleiro[0][0]=11;
						}
					}
				}
			}
			//pessoa2
			dadou=1+rand()%6;
			if(rodada<=9)
			{
				tabuleiro[0][dadou]=22;
			}
			else if(rodada>9 && rodada<=15)
			{
				tabuleiro[dadou][9]=22;
			}
			else if(rodada>15 && rodada<=24)
			{
				tabuleiro[6][dadou]=22;
			}
			else if(rodada>24)
			{
				tabuleiro[dadou][0]=22;
			}
				for(i=0;i<7;i++)
			{
				for(j=0;j<10;j++)
				{
					if(tabuleiro[i][j]!='*')
					{
						if(tabuleiro[i][j]==2)
						{
							for(i=0;i<3;i++)
							{ 
								vida2--;
							}
						}
						
						if(tabuleiro[i][j]==3)
						{
							vida2++;
						}
						
						if(tabuleiro[i][j]==4)
						{
							tabuleiro[i][j]='*';
						}
						if(tabuleiro[i][j]==5)
						{
							dadou=1+rand()%6;
								if(rodada<=9)
								{
									tabuleiro[0][dadou]=22;
								}
								else if(rodada>9 && rodada<=15)
								{
									tabuleiro[dadou][9]=22;
								}
								else if(rodada>15 && rodada<=24)
								{
									tabuleiro[6][dadou]=22;
								}
								else if(rodada>24)
								{
									tabuleiro[dadou][0]=22;
								}
						}
						if(tabuleiro[i][j]==6)
						{
							tabuleiro[0][0]=22;
						}
					}
				}
			}
			
		}
		else
		{
				//pessoa1
			dadou=1+rand()%6;
			if(rodada<=9)
			{
				tabuleiro[0][dadou]=11;
			}
			else if(rodada>9 && rodada<=15)
			{
				tabuleiro[dadou][9]=11;
			}
			else if(rodada>15 && rodada<=24)
			{
				tabuleiro[6][dadou]=11;
			}
			else if(rodada>24)
			{
				tabuleiro[dadou][0]=11;
			}
			
			
			for(i=0;i<7;i++)
			{
				for(j=0;j<10;j++)
				{
					if(tabuleiro[i][j]!='*')
					{
						if(tabuleiro[i][j]==2)
						{
							for(i=0;i<3;i++)
							{ 
								vida1--;
							}
						}
						
						if(tabuleiro[i][j]==3)
						{
							vida1++;
						}
						
						if(tabuleiro[i][j]==4)
						{
							tabuleiro[i][j]='*';
						}
						if(tabuleiro[i][j]==5)
						{
							dadou=1+rand()%6;
								if(rodada<=9)
								{
									tabuleiro[0][dadou]=11;
								}
								else if(rodada>9 && rodada<=15)
								{
									tabuleiro[dadou][9]=11;
								}
								else if(rodada>15 && rodada<=24)
								{
									tabuleiro[6][dadou]=11;
								}
								else if(rodada>24)
								{
									tabuleiro[dadou][0]=11;
								}
						}
						if(tabuleiro[i][j]==6)
						{
							tabuleiro[0][0]=11;
						}
					}
				}
			}
			//pessoa2
			dadou=1+rand()%6;
			if(rodada<=9)
			{
				tabuleiro[0][dadou]=22;
			}
			else if(rodada>9 && rodada<=15)
			{
				tabuleiro[dadou][9]=22;
			}
			else if(rodada>15 && rodada<=24)
			{
				tabuleiro[6][dadou]=22;
			}
			else if(rodada>24)
			{
				tabuleiro[dadou][0]=22;
			}
				for(i=0;i<7;i++)
			{
				for(j=0;j<10;j++)
				{
					if(tabuleiro[i][j]!='*')
					{
						if(tabuleiro[i][j]==2)
						{
							for(i=0;i<3;i++)
							{ 
								vida2--;
							}
						}
						
						if(tabuleiro[i][j]==3)
						{
							vida2++;
						}
						
						if(tabuleiro[i][j]==4)
						{
							tabuleiro[i][j]='*';
						}
						if(tabuleiro[i][j]==5)
						{
							dadou=1+rand()%6;
								if(rodada<=9)
								{
									tabuleiro[0][dadou]=22;
								}
								else if(rodada>9 && rodada<=15)
								{
									tabuleiro[dadou][9]=22;
								}
								else if(rodada>15 && rodada<=24)
								{
									tabuleiro[6][dadou]=22;
								}
								else if(rodada>24)
								{
									tabuleiro[dadou][0]=22;
								}
						}
						if(tabuleiro[i][j]==6)
						{
							tabuleiro[0][0]=22;
						}
					}
				}
			}
			
		}
			
		
		if(vida1==0)
		{
			vencedor=11;
			break;
		}
		if(vida1==0)
		{
			vencedor=11;
			break;
		}
		
	rodada++;
	}while(rodada!=27);
	
	if(vencedor==11)
	{
		printf("Pessoa 2 venceu");
		printf("Tinha %i vidas", vida2);
	}
	else if(vencedor==22)
	{
		printf("Pessoa 1 venceu", vida1);
		printf("Tinha %i vidas", vida1);
	}
	if(rodada==27)
	{
		if(tabuleiro[3][0]==11)
		{
			printf("Pessoa 1 venceu", vida1);
			printf("Tinha %i vidas", vida1);
		}
		else
		{
			printf("Pessoa 2 venceu");
			printf("Tinha %i vidas", vida2);
		}
		
	}
}

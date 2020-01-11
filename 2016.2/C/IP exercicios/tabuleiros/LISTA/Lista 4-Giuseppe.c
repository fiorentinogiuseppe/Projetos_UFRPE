/*
1
#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<math.h>
#include<time.h>

main()
{
	int tamanho, j, i;
	
	printf("Digite a o tamanho do vetor: ");
	scanf("%i",&tamanho);
	
	int v1[tamanho];
	int v2[tamanho];
	
	for(i=0; i<tamanho; i++)
	{
			printf("Digite o valor %i de v1 e v2: ", i);
			scanf("%i %i", &v1[i], &v2[i]);
	}
	for(i=0; i<tamanho; i++)
	{
		j= v1[i]*v2[i];
		printf("v1[%i] x v2[%i]= %i\n", v1[i], v2[i], j);
	}
}

2
#include<stdio.h>
#include<stdlib.h>

main()
{
	int tamanho,i, big=0, less=1000;
	printf("Digite o tamanho do vetor: ");
	scanf("%i", &tamanho);
	
	int m[tamanho];
	for(i=0; i<tamanho; i++)
	{
		printf("Digite o valor de m[%i]: ", i);
		scanf("%i", &m[i]);
	}
	for(i=0; i<tamanho;i++)
	{
		if(m[i]>big)
		{
			big=m[i];
		}
		else if(m[i]<less)
		{
			less=m[i];
		}
	}
	printf("O maior valor foi: %i\nO menor valor foi: %i", big, less);
}
3
#include<stdio.h>
#include<stdlib.h>
#include<string.h>

main()
{
	int tamanho,i,j,k,a;
	printf("Digite o tamanho dos vetores: ");
	scanf("%i", &tamanho);
	int v[tamanho];
	char s[tamanho];

	system("cls");
	printf("Digite os valores do vetor de inteiros!!\n");
	for(i=0;i<tamanho;i++)
	{
		printf("Digite v[%i]: ", i);
		scanf("%i", &v[i]);
	}
	system("cls");
	printf("Digite os valores da string\n");
	getchar();
	for(i=0;i<tamanho;i++)
	{
		printf("Digite o s[%i]: ",i);
		scanf("%c", &s[i]);
		getchar();
	}
	
		for(j=0; j<tamanho;j++)
	{
		a=v[j];
		for(k=0; k<a; k++)
		{
			printf("%c", s[k]);
		}
		printf("\n");
	}

}
4 possui funções e segundo o professor não era pra ser feito.
5
#include<stdio.h>
#include<stdlib.h>

main()
{
    //variaveis
    int i,j,v,w=9;
    int m[10][10];


    for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
                m[i][j]=0;
        }

    }
     for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
            if(j==w)
            printf("%i\t",m[i][j]);
            else if(j!=w)
            {
                printf("\t");
            }
        }
        w--;
        printf("\n");

    }
}
6
#include<stdio.h>
#include<stdlib.h>

main()
{
    //variaveis
    int i,j,v,w=9;
    int m[10][10];


    for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
                m[i][j]=0;
        }

    }
     for(i=0;i<10;i++)
    {
        for(j=0;j<10;j++)
        {
            if(j!=w)
            printf("%i\t",m[i][j]);
            else if(j==w)
            {
                printf("\t");
            }
        }
        w--;
        printf("\n");

    }
}
7
#include<stdio.h>
#include<stdlib.h>

main()
{
    int i,j,w;
    int m[6][6];

    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            m[i][j]=1;

        }
    }
    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            printf("%i",m[i][j]);

        }
        printf("\n");
    }
    printf("\n\n");
    for(i=0;i<6;i++)
    {
        for(j=0;j<6;j++)
        {
            w=i+j;
            if(w%2==0)
            printf("%i",m[i][j]);
            else
            {
                printf(" ");
            }

        }
        printf("\n");
    }

}
8

#include <stdio.h>
#include<stdlib.h>

main()
{
    int A[5][5], B[5][5], SOMA[5][5];
    int i,j;

    for(i=0;i<5;i++)
    {
        for(j=0;j<5;j++)
        {
            A[i][j]=1;
            printf("%i", A[i][j]);
        }
        printf("\n");
    }
    printf("\n");
    printf("\+");
    printf("\n");
    printf("\n");
    for(i=0;i<5;i++)
    {
        for(j=0;j<5;j++)
        {
            B[i][j]=2;
            printf("%i", B[i][j]);

        }
        printf("\n");

    }
    printf("\n");
    printf("\=");
    printf("\n");
    printf("\n");
    for(i=0;i<5;i++)
    {
        for(j=0;j<5;j++)
        {
            SOMA[i][j]=A[i][j]+B[i][j];
            printf("%i", SOMA[i][j]);

        }
        printf("\n");

    }
}
9
#include<stdio.h>
#include<stdlib.h>

main()
{
	int c[2][3], c_2[3][2];
	int i,j;
	
	printf("C2x3");
	printf("\n\n");
	for(i=0;i<2;i++)
	{
		for(j=0;j<3;j++)
		{
			c[i][j]= 0;
			printf("%i\t", c[i][j]);
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	printf("C3x2");

	printf("\n\n");
	for(i=0;i<3;i++)
	{
		for(j=0;j<2;j++)
		{
			c_2[i][j]= 0;
			printf("%i\t", c_2[i][j]);
		}
		printf("\n");
	}

}

10 Foi dito na sala que esta questão não era pra ser feita.
11
#include<stdio.h>
#include<stdlib.h>

main()
{
	int A[10], B[10];
	int i,valor;
	
	for(i=0;i<10;i++)
	{
		printf("Digite o valor de A[%i]: ",i);
		scanf("%i", &valor);
		
		A[i]=valor;
	}
	
	for(i=0;i<10;i++)
	{
		if(i%2==0)
		{
			(float) B[i], A[i];
			B[i]=A[i]/2;
		}
		else
		{
			B[i]=A[i]*3;
		}
	}
	
	for(i=0;i<10;i++)
	{
		printf("A[%i]= %i\n\nB[%i]= %i\n\n",i,A[i],i,B[i]);
	}
}
12
#include<stdio.h>
#include<stdlib.h>

main()
{
	int A[5], B[5], C[5];
    int i,j, resp;

	//Recebimento de Dados
	
        for(j=0;j<5;j++)
        {
        	printf("Digite o Valor do vetor A[%i]: ",j);
	        scanf("%i", &resp);
			A[j]=resp;
            
        }

    
 
        for(j=0;j<5;j++)
        {
        	printf("Digite o Valor da matriz B[%i]: ",j);
	        scanf("%i", &resp);
			B[j]=resp;
            
        }
        system("cls");
        

	//Print
   
        for(j=0;j<5;j++)
        {
            printf("%i\t", A[j]);
        }
        printf("\n");
  
    printf("\n");
    printf("\+");
    printf("\n");
    printf("\n");
	
	 for(j=0;j<5;j++)
        {
            printf("%i\t", B[j]);
        }

    printf("\n");
    printf("\=");
    printf("\n");
    printf("\n");
  
        for(j=0;j<5;j++)
        {
            C[j]=A[j]+B[j];
            printf("%i\t", C[j]);

        }

}
13
Resposta BOA SORTE!.
14
#include<stdio.h>
#include<stdlib.h>

main()
{
	float nota1[20];
	float nota2[20];
	float media[20];
	int i,j;


	for(i=0;i<20;i++)
	{
		    printf("Digite a nota 1 do aluno %i: ", i+1);
		    scanf("%f", &nota1[i]);
	}
    system("cls");
    for(i=0;i<20;i++)
	{
		    printf("Digite a nota 2 do aluno %i: ", i+1);
		    scanf("%f", &nota2[i]);
	}
    system("cls");
	 for(i=0;i<20;i++)
	{
        media[i]= ((nota1[i]*2)+(nota2[i]*3))/5;
        printf("A media do aluno %i: %.2f\n",i+1, media[i]);
	}
}
15
#include<stdio.h>
#include<stdlib.h>

main()
{
	float nota1[20];
	float nota2[20];
	float media[20];
	int i,j, contador=0;
	float mediaturma=0, mediaturmafinal;


	for(i=0;i<5;i++)
	{
		    printf("Digite a nota 1 do aluno %i: ", i+1);
		    scanf("%f", &nota1[i]);
	}
    system("cls");
    for(i=0;i<5;i++)
	{
		    printf("Digite a nota 2 do aluno %i: ", i+1);
		    scanf("%f", &nota2[i]);
	}
    system("cls");
	 for(i=0;i<5;i++)
	{
        media[i]= ((nota1[i]*2)+(nota2[i]*3))/5;
        printf("A media do aluno %i: %.2f\n",i+1, media[i]);
	}
	for(i=0;i<5;i++)
	{
		mediaturma+= media[i];
	}
	mediaturmafinal=mediaturma/5;
	printf("os alunos que ficaram abaixo da media foram: \n");
	for(i=0;i<5;i++)
	{
		if(media[i] < mediaturmafinal)
		{
			contador++;
			printf("o aluno %i;\n",i);
		}
	}
	printf("\nUm total de %i alunos", contador);
}

16
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
17
Resposta
012
123

18
#include<stdio.h>
#include<stdlib.h>

main()
{
	int m[4][4];
	
	int i,j;
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<4;j++)
		{
			printf("Digite o valor de m[%i][%i]", i,j);
			scanf("%i",&m[i][j]);
		}
	}
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<4;j++)
		{
			printf("%i\t", m[i][j]);
		}
		printf("\n");
	}
	
	system("pause");
	system("cls");
	
	for(i=0;i<4;i++)
	{
		for(j=0;j<3;j++)
		{
			printf("%i\t", m[j][i]);
		}
		printf("\n");
	}
}

19
#include <stdio.h>
#include<stdlib.h>

main()
{
    int A[3][3], B[3][3], Produto[3][3];
    int i,j;

    for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
            A[i][j]=i;
            printf("%i", A[i][j]);
        }
        printf("\n");
    }
    printf("\n");
    printf("\X");
    printf("\n");
    for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
            B[i][j]=i;
            printf("%i", B[i][j]);

        }
        printf("\n");

    }
     printf("\n");
    printf("\=");
    printf("\n");
     for(i=0;i<3;i++)
    {
        for(j=0;j<3;j++)
        {
			Produto[i][j]= A[i][j]*B[j][i];            
			printf("%i", Produto[i][j]);

        }
        printf("\n");

    }
}
20
#include<stdio.h>
#include<stdlib.h>

main()
{
	int m[5][5];
	int i,j,w=4,v=1;
	
	for(i=0;i<5;i++)
	{
		for(j=0;j<5;j++)
		{
			m[i][j]=0;
		}
	}	
	//Diagonal principal
	for(i=0;i<5;i++)
	{
		for(j=0;j<5;j++)
		{
			if(i==j)
			printf("%i\t",m[i][j]);
			else
			{
				printf("*\t");
			}
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	//Tudo memos a diagonal princial
	for(i=0;i<5;i++)
	{	
		v++;
		for(i=0;i<=v;i++)
		printf("*");
		for(j=0;j<w;j++)
		{
			printf("%i",m[i][j+1]);
			
		}
		printf("\n");
		
		w--;
	}

}
21
Resposta:
SERA
QUE*
*E**
ISTO

22
#include<stdio.h>
#include<stdlib.h>
#include<time.h>

main()
{
	float m[10][10];
	int i,j, diagonalprinciapal=0;
	srand(time(NULL));
	
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			
			m[i][j]=rand()%10;	
		}
	}
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			printf("%.1f\t", m[i][j]);
				
		}
		printf("\n");
	}
	system("pause");
	system("cls");
	
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			if(i==j)
			{
				diagonalprinciapal+=m[i][j];
				m[i][j]=diagonalprinciapal;
				printf("%i ", diagonalprinciapal);
			}
			else
			{
				printf(" ");
			}
		}
		printf("\n");
	}
	
	system("pause");
	system("cls");
	printf("\n");
	for(i=1;i<10;i++)
	{
		for(j=0;j<i;j++)
		{
			m[i][j]= m[i][j]*diagonalprinciapal;
		}
		
	}
	for(i=0;i<10;i++)
	{
		for(j=0;j<10;j++)
		{
			printf("%.1f\t", m[i][j]);
		}
		printf("\n");
	}
	
	
}
23
*/

#include<stdio.h>
#include<stdlib.h>

main()
{
	int v[9];
	int m[3][3];
	int i,j,z=0;
	
	for(i=0;i<9;i++)
	{
		printf("Digite o valor %i da matriz: ",i, v[i]);
		scanf("%i",&v[i]);
	}
	for(i=0;i<9;i++)
	{
		printf("%i", v[i]);
	}
	
	for(i=0;i<3;i++)
	{
		for(j=0;j<3;j++)
		{
			m[j][i]= v[z];
			z++;
		}
	}
	printf("\n");
	for(i=0;i<3;i++)
	{
		for(j=0;j<3;j++)
		{
			printf("%i\t", m[i][j]);
		}
		printf("\n");
	}
	
}

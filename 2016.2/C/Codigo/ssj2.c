#include <stdio.h>
#include <stdlib.h>
#include <conio.h>
#include <stdbool.h>
#include <windows.h>
#include <process.h> //para usar o _beginthread()
#include <string.h>

typedef struct{
    char usuario[10];
    char senha[10];
    char nome[50];
}TUsuario;

typedef struct{
    char nome[200];
    char data_nascimento[10];
    char RG[9];
    char CPF[14];
    char email[100];
    char telefone[13];
    char endereco[200];
    float salario;

    char usuario[10];
    char senha[10];
}TFuncionario;

typedef struct{
    char nome[10];
    char senha[10];
}TProduto;
void Musica_opcao()
{
    Beep(380,300);
}
void Musica(){
     //musica do salto do jogo
    Beep(860,500);
}
void gotoxy(int x,int y)
{	//pra posicionar com coordenadas
    COORD p={x,y};
	SetConsoleCursorPosition(GetStdHandle(STD_OUTPUT_HANDLE),p);
}
void textcolor(int newcolor)
{   //pra mudar a cor da letra
   CONSOLE_SCREEN_BUFFER_INFO csbi;

   GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &csbi);
   SetConsoleTextAttribute(GetStdHandle(STD_OUTPUT_HANDLE),
      (csbi.wAttributes & 0xf0) | newcolor);

}
void textbackground(int newcolor)
{   //pra mudar a cor de fundo da letra
   CONSOLE_SCREEN_BUFFER_INFO csbi;

   GetConsoleScreenBufferInfo(GetStdHandle(STD_OUTPUT_HANDLE), &csbi);
   SetConsoleTextAttribute(GetStdHandle(STD_OUTPUT_HANDLE),
      (csbi.wAttributes & 0x0f) | (newcolor << 4));

}
void Janela()
{   //desenho da borda da janela
    int i,j;
    for(i=1;i<27;i++)
    {
        gotoxy(0,i);
        printf("%c",186);
    }
    gotoxy(0,27);
    printf("%c",200);
    gotoxy(0,0);
    printf("%c",201);
    for(i=1;i<121;i++)
    {
        gotoxy(i,0);
        printf("%c",205);
    }
    for(i=1;i<121;i++)
    {
        gotoxy(i,27);
        printf("%c",205);
    }

    gotoxy(121,27);
    printf("%c",188);

    gotoxy(121,0);
    printf("%c",187);
    for(i=1;i<27;i++)
    {
        gotoxy(121,i);
        printf("%c",186);
    }

}
void Cadeado_Fechado()
{
    int i=3,j=38;
    gotoxy(j,i);i++;
    printf("               ");
    gotoxy(j,i);i++;
    printf("                  ");
    gotoxy(j,i);i++;
    printf("     .--------.");
    gotoxy(j,i);i++;
    printf("    / .------. \\ ");
    gotoxy(j,i);i++;
    printf("   /  /       \\ \\");
    gotoxy(j,i);i++;
    printf("   | |        | |");
    gotoxy(j,i);i++;
    printf("  _| |________| |_");
    gotoxy(j,i);i++;
    printf(".' |_|        |_| '.");
    gotoxy(j,i);i++;
    printf("'._____ ____ _____.'");
    gotoxy(j,i);i++;
    printf("|     .'____'.     |");
    gotoxy(j,i);i++;
    printf("'.__.'.'    '.'.__.'");
    gotoxy(j,i);i++;
    printf("'.__  |STEAM |  __.'");
    gotoxy(j,i);i++;
    printf("|   '.'.____.'.'   |");
    gotoxy(j,i);i++;
    printf("'.____'.____.'____.'");
    gotoxy(j,i);i++;
    printf("'.________________.'");

}
void Cadeado_abrindo()
{
    int i=3,j=38;

    gotoxy(j,i);i++;
    printf("               ");
    gotoxy(j,i);i++;
    printf("                  ");
    gotoxy(j,i);i++;
    printf("     .--------.");
    gotoxy(j,i);i++;
    printf("    / .------. \\ ");
    gotoxy(j,i);i++;
    printf("   /  /       \\ \\");
    gotoxy(j,i);i++;
    printf("   | |        | |");
    gotoxy(j,i);i++;
    printf("  _| |________| |_");
    gotoxy(j,i);i++;
    printf(".' |_|        |_| '.");
    gotoxy(j,i);i++;
    printf("'._____ ____ _____.'");
    gotoxy(j,i);i++;
    printf("|     .'____'.     |");
    gotoxy(j,i);i++;
    printf("'.__.'.'    '.'.__.'");
    gotoxy(j,i);i++;
    printf("'.__  |STEAM |  __.'");
    gotoxy(j,i);i++;
    printf("|   '.'.____.'.'   |");
    gotoxy(j,i);i++;
    printf("'.____'.____.'____.'");
    gotoxy(j,i);i++;
    printf("'.________________.'");

    Sleep(500);

     i=3,j=38;


    gotoxy(j,i);i++;
    printf("               ");
    gotoxy(j,i);i++;
    printf("                  ");
    gotoxy(j,i);i++;
    printf("                     ");
    gotoxy(j,i);i++;
    printf("                 ");
    gotoxy(j,i);i++;
    printf("                  ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");
    gotoxy(j,i);i++;
    printf("                    ");

     i=3,j=38;

    gotoxy(j,i);i++;
    printf("     .--------.");
    gotoxy(j,i);i++;
    printf("    / .------. \\ ");
    gotoxy(j,i);i++;
    printf("   \\/ \\/        \\ \\");
    gotoxy(j,i);i++;
    printf("   | |        | |");
    gotoxy(j,i);i++;
    printf("   | |        |_|");
    gotoxy(j,i);i++;
    printf("   | |            ");
    gotoxy(j,i);i++;
    printf("  _| |____________");
    gotoxy(j,i);i++;
    printf(".' |_|            '.");
    gotoxy(j,i);i++;
    printf("'._____ ____ _____.'");
    gotoxy(j,i);i++;
    printf("|     .'____'.     |");
    gotoxy(j,i);i++;
    printf("'.__.'.'    '.'.__.'");
    gotoxy(j,i);i++;
    printf("'.__  |STEAM |  __.'");
    gotoxy(j,i);i++;
    printf("|   '.'.____.'.'   |");
    gotoxy(j,i);i++;
    printf("'.____'.____.'____.'");
    gotoxy(j,i);i++;
    printf("'.________________.'");


    Sleep(500);
}
void Login()
{
    system("cls");
    FILE*p;
    fpos_t Byte;

    TFuncionario funcionario;
    char usuario[10];
    char senha[10];

    Janela();
    system("color 3F");
    //Janela do usuario e senha
    {
        int i;

    gotoxy(9,11);
    printf("%c",186);

    gotoxy(9,12);
    printf("%c",204);
    gotoxy(9,10);
    printf("%c",201);
    for(i=10;i<35;i++)
    {
        gotoxy(i,10);
        printf("%c",205);
    }
    for(i=10;i<35;i++)
    {
        gotoxy(i,12);
        printf("%c",205);
    }

    gotoxy(35,12);
    printf("%c",185);

    gotoxy(35,10);
    printf("%c",187);

    gotoxy(35,11);
    printf("%c",186);

    //Janela senha
    for(i=10;i<35;i++)
    {
        gotoxy(i,14);
        printf("%c",205);
    }

    gotoxy(35,14);
    printf("%c",188);

    gotoxy(35,13);
    printf("%c",186);

    gotoxy(9,13);
    printf("%c",186);

    gotoxy(9,14);
    printf("%c",200);

    }



    gotoxy(10,11);
    printf("Usuario:");
    gotoxy(10,13);
    printf("Senha:");

    Cadeado_Fechado();

    int cont_senha=0,cont_usuario=0,i,j,achou=0;

    do
    {
        cont_senha=0;

        for(i=0;i<10;i++)
        {
           senha[i]=' ';
           usuario[i]=' ';
        }
        //USUARIO
        gotoxy(19,11);
        gets(usuario);
        //SENHA
        j=17;
        for(i=0;i<10;i++)
        {

            gotoxy(j,13);
            senha[i]=getch();

            if(senha[i]==13)
            {
                break;
            }
            else if(senha[i]==8)
            {
                if(i>0)
                {
                    senha[i]=' ';
                    gotoxy(j-1,13);
                    printf(" ");
                    j--;
                    i-=2;
                    cont_senha--;
                }
                else
                {
                    i--;
                }

            }

            else
            {
                cont_senha++;
                j++;
               printf("*");
            }
            if(j<17)
            {
                j=17;
            }

        }


        p=fopen("Funcionario","rb");
        fread(&funcionario,sizeof(funcionario),1,p);
        while(!feof(p)&&achou==0)
        {
            if(strcmp(funcionario.usuario,usuario)==0)
            {   j=0;
                for(i=0;i<cont_senha;i++)
                {
                    if(funcionario.senha[i]!=senha[i])
                    {
                        j++;
                    }
                }
                if(j==0)
                {
                    achou=1;
                    Cadeado_abrindo();
                    break;
                }
            }
            fread(&funcionario,sizeof(funcionario),1,p);
        }
            fclose(p);

        if(achou==0)
        {
            gotoxy(10,15);
            printf("Usuario ou senha incorretos.");
            gotoxy(10,16);
            printf("Tente novamente\n\n\n\n\n\n");

            Sleep(1000);
            //Para apagar as linhas
            gotoxy(19,11);
            printf("              ");
            gotoxy(17,13);
            printf("               ");
            gotoxy(10,15);
            printf("                            ");
            gotoxy(10,16);
            printf("               ");
        }
    }while(achou==0);
}
void Jogo()
{char opcao;


    textcolor(0);

    system("color 3F");

    bool fecha=true;
    int i,j,correcao_coluna=30,posicao_placar_vida=30+correcao_coluna;

        //desenho da grama
        textbackground(15);
        textcolor(2);
        gotoxy(9+correcao_coluna,23);
        for(i=9+correcao_coluna;i<52+correcao_coluna;i++)
        {
            gotoxy(i,23);
            printf("%c",178);
        }
        int x=55,var=0,num=0,points=0, erro=0,vida=3, mais_coracao=200,coracao_posi=51+correcao_coluna,ativar_vida,erro_C=0;

do
{



    while(!kbhit())
    {



        //vidas

            gotoxy(posicao_placar_vida,19);
            printf("     ");

        for(i=0;i<vida;i++)
        {
            textcolor(4);
            gotoxy(posicao_placar_vida,19);
            printf("%c",3);

            posicao_placar_vida+=2;
        }
        textcolor(15);
        posicao_placar_vida=30+correcao_coluna;

        //pontua��o
        textbackground(3);
        textcolor(15);
        gotoxy(18+correcao_coluna,19);
        printf("            ",points);
        gotoxy(18+correcao_coluna,19);
        printf("Points:%i",points);

        if(vida>0)
        {

        //Boneco pulando
        if(opcao==' ')
        {


            gotoxy(10+correcao_coluna,21);
            printf(" ");
            gotoxy(11+correcao_coluna,21);
            printf(" ");
            gotoxy(12+correcao_coluna,21);
            printf(" ");
            gotoxy(10+correcao_coluna,22);
            printf(" ");
            gotoxy(12+correcao_coluna,22);
            printf(" ");

            textcolor(15);
            gotoxy(11+correcao_coluna,19);
            printf("%c",1);
            gotoxy(10+correcao_coluna,19);
            printf("\\");
            gotoxy(12+correcao_coluna,19);
            printf("/");
            gotoxy(10+correcao_coluna,20);
            printf("_");
            gotoxy(11+correcao_coluna,20);
            printf("|");
            gotoxy(12+correcao_coluna,20);
            printf("_");

            if(x==11+correcao_coluna)
            {
                points++;
            }



        }//Boneco normal
        else
        {
            gotoxy(11+correcao_coluna,19);
            printf(" ");
            gotoxy(10+correcao_coluna,19);
            printf(" ");
            gotoxy(12+correcao_coluna,19);
            printf(" ");
            gotoxy(10+correcao_coluna,20);
            printf(" ");
            gotoxy(12+correcao_coluna,20);
            printf(" ");

            textcolor(15);
            gotoxy(11+correcao_coluna,20);
            printf("%c",1);
            gotoxy(10+correcao_coluna,21);
            printf("/");
            gotoxy(11+correcao_coluna,21);
            printf("|");
            gotoxy(12+correcao_coluna,21);
            printf("\\");
            gotoxy(10+correcao_coluna,22);
            printf("/");
            gotoxy(12+correcao_coluna,22);
            printf("\\");

            if(erro==0)
            {
                if(x==11+correcao_coluna||x==10+correcao_coluna||x==12+correcao_coluna)
                {
                        gotoxy(18+correcao_coluna,20);
                        printf("You hit the block. -2pts.");
                        points-=2;
                        vida--;

                    erro++;
                }
            }
            if(erro_C==0)
            {
                if(coracao_posi==11+correcao_coluna||coracao_posi==10+correcao_coluna||coracao_posi==12+correcao_coluna)
                {
                    if(vida<3)
                    {
                        gotoxy(18+correcao_coluna,21);
                        textcolor(4);
                        printf("%c++",3);
                        textcolor(15);
                        vida++;

                        erro_C++;
                    }
                }
            }


        }
        if(x<10+correcao_coluna)
        {
            erro=0;
        }
        if(x==16+correcao_coluna)
        {
            gotoxy(18+correcao_coluna,20);
            printf("                         ");

            gotoxy(18+correcao_coluna,21);
            printf("    ");
        }


        if(x>correcao_coluna)
        {
          x--; gotoxy(x,22); printf("^");
          gotoxy(x+1,22); printf(" ");
        }
        if(x==8+correcao_coluna)
        {   gotoxy(x,22);
            printf(" ");
            x=51+correcao_coluna;
        }
        //A variavel var vai fazer com que o boneco passe um tempo no ar quando pula
        if(var==5)
        {
            var=0;
            opcao='a';
        }
        var++;
        mais_coracao--;

        if(mais_coracao==0)
        {
            mais_coracao=200;
            ativar_vida=1;
            erro_C=0;

        }


        if(coracao_posi>correcao_coluna&&ativar_vida==1)
        {
          coracao_posi--; gotoxy(coracao_posi,22); textcolor(4);printf("%c",3);textcolor(15);
          gotoxy(coracao_posi+1,22); printf(" ");
        }
        if(coracao_posi==8+correcao_coluna)
        {   gotoxy(coracao_posi,22);
            printf(" ");
            coracao_posi=51+correcao_coluna;
            ativar_vida=0;
        }

        }
        else
        {
            gotoxy(20+correcao_coluna,21);
            textcolor(4);
            printf("Voce morreu! Deseja jogar novamente?(S/N)");
            textcolor(15);
        }

        Sleep(50);


    }

        opcao=getch();

        if(opcao=='s'||opcao=='S'||vida>0)
        {
            var=0;

            _beginthread(Musica,0,NULL);
            if(vida==0)
            {
                gotoxy(20+correcao_coluna,21);
                printf("                                         ");
                gotoxy(x,22);
                printf(" ");
                x=51+correcao_coluna;
                points=0;
                erro=0;
                vida=3;
                erro_C=0;
            }

        }

        if(opcao=='x'||opcao=='X'||opcao=='n'||opcao=='N')
        {
            fecha=false;
            break;
        }





}while(fecha);

}
void Tela_de_Carregamento()
{

    char opcao;
    int i,j,x=55,var=0, percentual=45,num=0,p=0,points=0, erro=0;
    bool fecha=true;
    textcolor(0);
    //Cor_do_BG(15);
    system("color 3F");
    Janela();
    Logo(50,1);

    int correcao_coluna=30,vida=3,posicao_placar_vida=30+correcao_coluna;

    //DESENHO DA BORDA DO CARREGAMENTO
    {for(i=15+correcao_coluna;i<42+correcao_coluna;i++)
            {
                gotoxy(i,15);
                printf("%c",205);
            }
            gotoxy(14+correcao_coluna,15);
            printf("%c",201);


            gotoxy(42+correcao_coluna,15);
            printf("%c",187);

            gotoxy(14+correcao_coluna,16);
            printf("%c",186);

            for(i=15+correcao_coluna;i<42+correcao_coluna;i++)
            {
                gotoxy(i,17);
                printf("%c",205);
            }

            gotoxy(42+correcao_coluna,17);
            printf("%c",188);

            gotoxy(14+correcao_coluna,17);
            printf("%c",200);

            gotoxy(42+correcao_coluna,16);
            printf("%c",186);
    }

        //desenho da grama
        textbackground(15);
        textcolor(2);
        gotoxy(9+correcao_coluna,23);
        for(i=9+correcao_coluna;i<52+correcao_coluna;i++)
        {
            gotoxy(i,23);
            printf("%c",178);
        }

do
{


    while(!kbhit()&&num<=100)
    {

        //carregamento
        if(num<=100)
        {
            textbackground(7);
            textcolor(7);
            gotoxy(percentual,16);
            printf("%c",219);
            textcolor(15);
            textbackground(3);
            gotoxy(44+correcao_coluna,16);
            printf("%i%%",num);

            num++;
            if(num%3==0)
            {
               p++;
            }

            if(p%4==0)
            {
                percentual++;
            }


        }


        //vidas

            gotoxy(posicao_placar_vida,19);
            printf("     ");

        for(i=0;i<vida;i++)
        {
            textcolor(4);
            gotoxy(posicao_placar_vida,19);
            printf("%c",3);

            posicao_placar_vida+=2;
        }
        textcolor(15);
        posicao_placar_vida=30+correcao_coluna;


        //pontua��o
        textbackground(3);
        textcolor(15);
        gotoxy(18+correcao_coluna,19);
        printf("            ",points);
        gotoxy(18+correcao_coluna,19);
        printf("Points:%i",points);
        if(vida>0)
        {




        //Boneco pulando
        if(opcao==' ')
        {


            gotoxy(10+correcao_coluna,21);
            printf(" ");
            gotoxy(11+correcao_coluna,21);
            printf(" ");
            gotoxy(12+correcao_coluna,21);
            printf(" ");
            gotoxy(10+correcao_coluna,22);
            printf(" ");
            gotoxy(12+correcao_coluna,22);
            printf(" ");

            textcolor(15);
            gotoxy(11+correcao_coluna,19);
            printf("%c",1);
            gotoxy(10+correcao_coluna,19);
            printf("\\");
            gotoxy(12+correcao_coluna,19);
            printf("/");
            gotoxy(10+correcao_coluna,20);
            printf("_");
            gotoxy(11+correcao_coluna,20);
            printf("|");
            gotoxy(12+correcao_coluna,20);
            printf("_");

            if(x==11+correcao_coluna)
            {
                points++;
            }



        }//Boneco normal
        else
        {
            gotoxy(11+correcao_coluna,19);
            printf(" ");
            gotoxy(10+correcao_coluna,19);
            printf(" ");
            gotoxy(12+correcao_coluna,19);
            printf(" ");
            gotoxy(10+correcao_coluna,20);
            printf(" ");
            gotoxy(12+correcao_coluna,20);
            printf(" ");

            textcolor(15);
            gotoxy(11+correcao_coluna,20);
            printf("%c",1);
            gotoxy(10+correcao_coluna,21);
            printf("/");
            gotoxy(11+correcao_coluna,21);
            printf("|");
            gotoxy(12+correcao_coluna,21);
            printf("\\");
            gotoxy(10+correcao_coluna,22);
            printf("/");
            gotoxy(12+correcao_coluna,22);
            printf("\\");

            if(erro==0)
            {
                if(x==11+correcao_coluna||x==10+correcao_coluna||x==12+correcao_coluna)
                {
                        gotoxy(18+correcao_coluna,20);
                        printf("You hit the block. -2pts.");
                        points-=2;
                        vida--;

                    erro++;
                }
            }


        }
        if(x<10+correcao_coluna)
        {
            erro=0;
        }
        if(x==16+correcao_coluna)
        {
            gotoxy(18+correcao_coluna,20);
            printf("                         ");
        }


        if(x>correcao_coluna)
        {
          x--; gotoxy(x,22); printf("^");
          gotoxy(x+1,22); printf(" ");
        }
        if(x==8+correcao_coluna)
        {   gotoxy(x,22);
            printf(" ");
            x=51+correcao_coluna;
        }
        //A variavel var vai fazer com que o boneco passe um tempo no ar quando pula
        if(var==5)
        {
            var=0;
            opcao='a';
        }
        var++;
        }
        else
        {
            gotoxy(30+correcao_coluna,21);
            textcolor(4);
            printf("Voce morreu!");
            textcolor(15);
        }

        Sleep(50);


    }
    if(num<100&&vida>0)
    {
        opcao=getch();
        var=0;
        _beginthread(Musica,0,NULL);
    }


}while(num<100);

    gotoxy(14+correcao_coluna,18);
    printf("O CARREGAMENTO FOI FINALIZADO.");
    gotoxy(18+correcao_coluna,20);
    printf("DESEJA CONTINUAR A JOGAR?(S/N)");
    do
    {
        opcao=getch();


        if(opcao=='s'||opcao=='S')
        {
            system("cls");

            p=0;
            percentual=45;


        //DESENHO DA BORDA DO CARREGAMENTO
    {for(i=15+correcao_coluna;i<42+correcao_coluna;i++)
            {
                gotoxy(i,15);
                printf("%c",205);
            }
            gotoxy(14+correcao_coluna,15);
            printf("%c",201);


            gotoxy(42+correcao_coluna,15);
            printf("%c",187);

            gotoxy(14+correcao_coluna,16);
            printf("%c",186);

            for(i=15+correcao_coluna;i<42+correcao_coluna;i++)
            {
                gotoxy(i,17);
                printf("%c",205);
            }

            gotoxy(42+correcao_coluna,17);
            printf("%c",188);

            gotoxy(14+correcao_coluna,17);
            printf("%c",200);

            gotoxy(42+correcao_coluna,16);
            printf("%c",186);
    }

            for(percentual=45;percentual<72;percentual++)
            {       textbackground(7);
                    textcolor(7);
                    gotoxy(percentual,16);
                    printf("%c",219);

            }
            textbackground(3);
            textcolor(15);
            num=100;
            gotoxy(44+correcao_coluna,16);
            printf("%i%%",num);
            gotoxy(15+correcao_coluna,25);
        printf("Para parar de jogar aperte X");

        Logo(50,1);
        Jogo();
        fecha=false;


    }
    else if(opcao=='n'||opcao=='N')
    {
        fecha=false;
    }

    }while(fecha);

}
void Quadro_colorido(int cor,int x_comeco,int x,int y_comeco, int y)
{
    int i,j;

    textbackground(cor);
    for(i=y_comeco;i<y;i++)
    {
        for(j=x_comeco;j<x;j++)
        {
            gotoxy(j,i);
            printf(" ");
        }

    }
}
void Area_de_Trabalho()
{   //area que tem os menus

    system("cls");
    Janela();
    system("Color 3F");
    Logo(50,12);

    POINT Coordenadas;
    int ativou=0,att1=0,att2=0,att3=0,att4=0,att5=0,x_antigo,y_antigo;

    //as variaveis att sao pra o menu n ficar piscando, ai so imprime uma vez e pronto e as variaveis com o nome entra tbm sao


    int movimento=0,movimentosubir=0,auxiliar,x,y,i,j;
    bool fecha=true,entra1=true,entra2=true;
    char resp;

                    Quadro_colorido(7,1,121,3,6);

					gotoxy(6,4);
					printf("%c",175);
					x=10;

			do
            {
                    textcolor(15);



                    GetCursorPos(&Coordenadas);

                    gotoxy(2,2);
                    printf("X: %d Y: %d", Coordenadas.x, Coordenadas.y);

                    textbackground(7);
                    //FUNCIONARIOS
                    if((Coordenadas.x>=60&&Coordenadas.x<=270 && Coordenadas.y<=166&&Coordenadas.y>=100))
                    {   att1=0;


                            Quadro_colorido(8,5,25,3,10);
                            gotoxy(10,4);
                            printf("FUNCIONARIOS");
                            do
                            {
                                if(kbhit())
                                {
                                    resp=getch();
                                }

                                    x_antigo=Coordenadas.x;
                                    y_antigo=Coordenadas.y;
                                    GetCursorPos(&Coordenadas);
                                    gotoxy(2,2);
                                    printf("X: %d Y: %d", Coordenadas.x, Coordenadas.y);

                                                    textbackground(8);
                                                 if((Coordenadas.x>=60&&Coordenadas.x<=270 && Coordenadas.y<=210&&Coordenadas.y>=170))
                                                 {
                                                    y=6;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,5,25,6,8);
                                                        gotoxy(10,6);
                                                        printf("CADASTRAR");
                                                        textbackground(8);
                                                    }

                                                    if(resp==13)
                                                    {
                                                        //so pra testar
                                                        Aba_Cadastro(1);
                                                        resp=' ';
                                                        Area_de_Trabalho();
                                                        break;
                                                    }


                                                 }
                                                 else if(x_antigo>=60&&x_antigo<=270 && y_antigo<=210&&y_antigo>=170)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,5,25,6,8);
                                                    gotoxy(10,6);
                                                    printf("CADASTRAR");
                                                 }
                                                 else if(entra1)
                                                 {
                                                    entra1=false;
                                                    gotoxy(10,6);
                                                    printf("CADASTRAR");
                                                 }


                                                if((Coordenadas.x>=60&&Coordenadas.x<=270 && Coordenadas.y<=260&&Coordenadas.y>=210))
                                                 {
                                                    y=8;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,5,25,8,10);
                                                        gotoxy(10,8);
                                                        printf("CONSULTAR");
                                                        textbackground(8);
                                                    }
                                                    if(resp==13)
                                                    {
                                                        //so pra testar
                                                        Aba_Consulta(1);
                                                        resp=' ';
                                                        Area_de_Trabalho();
                                                        break;
                                                    }



                                                 }
                                                 else if(x_antigo>=60&&x_antigo<=270 && y_antigo<=260&&y_antigo>=210)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,5,25,8,10);
                                                    gotoxy(10,8);
                                                    printf("CONSULTAR");
                                                 }
                                                 else if(entra2)
                                                 {
                                                     entra2=false;
                                                    gotoxy(10,8);
                                                    printf("CONSULTAR");
                                                 }


                    }while((Coordenadas.x>=60&&Coordenadas.x<=278 && Coordenadas.y<=260&&Coordenadas.y>=100));

                    Quadro_colorido(7,5,25,3,6);
                    Quadro_colorido(3,5,25,6,10);

                    entra1=true;entra2=true;

                    textbackground(7);

                    gotoxy(10,4);
                    printf("FUNCIONARIOS");
                    att1++;
                    }
                    else if(att1==0)
                    {
                        textbackground(7);
                        att1++;
                        gotoxy(10,4);
                        printf("FUNCIONARIOS");

                    }

                    //USUARIOS
                    if((Coordenadas.x>=270&&Coordenadas.x<=480 && Coordenadas.y<=166&&Coordenadas.y>=100))
                    {   att2=0;


                            Quadro_colorido(8,25,45,3,10);
                            gotoxy(30,4);
                            printf("USUARIOS");
                            do
                            {

                                    x_antigo=Coordenadas.x;
                                    y_antigo=Coordenadas.y;
                                    GetCursorPos(&Coordenadas);
                                    gotoxy(2,2);
                                    printf("X: %d Y: %d", Coordenadas.x, Coordenadas.y);

                                                    textbackground(8);
                                                 if((Coordenadas.x>=270&&Coordenadas.x<=480 && Coordenadas.y<=210&&Coordenadas.y>=170))
                                                 {
                                                     y=6;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,25,45,6,8);
                                                        gotoxy(30,6);
                                                        printf("CADASTRAR");
                                                        textbackground(8);
                                                    }



                                                 }
                                                 else if(x_antigo>=270&&x_antigo<=480 && y_antigo<=210&&y_antigo>=170)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,25,45,6,8);
                                                    gotoxy(30,6);
                                                    printf("CADASTRAR");
                                                 }
                                                 else if(entra1)
                                                 {
                                                    entra1=false;
                                                    gotoxy(30,6);
                                                    printf("CADASTRAR");
                                                 }


                                                if((Coordenadas.x>=270&&Coordenadas.x<=480 && Coordenadas.y<=260&&Coordenadas.y>=210))
                                                 {
                                                    y=8;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,25,45,8,10);
                                                        gotoxy(30,8);
                                                        printf("CONSULTAR");
                                                        textbackground(8);
                                                    }




                                                 }
                                                 else if(x_antigo>=270&&x_antigo<=480 && y_antigo<=260&&y_antigo>=210)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,25,45,8,10);
                                                    gotoxy(30,8);
                                                    printf("CONSULTAR");
                                                 }
                                                 else if(entra2)
                                                 {
                                                     entra2=false;
                                                    gotoxy(30,8);
                                                    printf("CONSULTAR");
                                                 }



                                                gotoxy(0,25);
                                                if(kbhit())
                                                {
                                                    resp=getch();
                                                }



                                                if(resp==13)
                                                {
                                                    switch(y)
                                                    {
                                                    case 6:
                                                        {
                                                        	Aba_Cadastro(2);
                                                        	resp=' ';
                                                        	Area_de_Trabalho();
                                                           break;
                                                        }
                                                    case 8:
                                                        {
                                                            Aba_Consulta(2);
                                                        	resp=' ';
                                                        	Area_de_Trabalho();
                                                            break;
                                                        }

                                                    }
                                                }

                    }while((Coordenadas.x>=270&&Coordenadas.x<=488 && Coordenadas.y<=260&&Coordenadas.y>=100));

                    Quadro_colorido(7,25,45,3,6);
                    Quadro_colorido(3,25,45,6,10);

                    entra1=entra2=true;

                        textbackground(7);
                        att2++;
                        gotoxy(30,4);
                        printf("USUARIOS");

                    }
                    else if(att2==0)
                    {
                        textbackground(7);
                        att2++;
                        gotoxy(30,4);
                        printf("USUARIOS");

                    }

                    //PRODUTOS
                    if((Coordenadas.x>=480&&Coordenadas.x<=690 && Coordenadas.y<=166&&Coordenadas.y>=100))
                    {   att3=0;


                            Quadro_colorido(8,45,65,3,10);
                            gotoxy(50,4);
                            printf("PRODUTOS");
                            do
                            {

                                    x_antigo=Coordenadas.x;
                                    y_antigo=Coordenadas.y;
                                    GetCursorPos(&Coordenadas);
                                    gotoxy(2,2);
                                    printf("X: %d Y: %d", Coordenadas.x, Coordenadas.y);

                                                    textbackground(8);
                                                 if((Coordenadas.x>=480&&Coordenadas.x<=690 && Coordenadas.y<=210&&Coordenadas.y>=170))
                                                 {
                                                     y=6;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,45,65,6,8);
                                                        gotoxy(50,6);
                                                        printf("CADASTRAR");
                                                        textbackground(8);
                                                    }


                                                 }
                                                 else if(x_antigo>=480&&x_antigo<=690 && y_antigo<=210&&y_antigo>=170)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,45,65,6,8);
                                                    gotoxy(50,6);
                                                    printf("CADASTRAR");
                                                 }
                                                 else if(entra1)
                                                 {
                                                     entra1=false;
                                                    gotoxy(50,6);
                                                    printf("CADASTRAR");
                                                 }


                                                if((Coordenadas.x>=480&&Coordenadas.x<=690 && Coordenadas.y<=260&&Coordenadas.y>=210))
                                                 {
                                                    y=8;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,45,65,8,10);
                                                        gotoxy(50,8);
                                                        printf("CONSULTAR");
                                                        textbackground(8);
                                                    }



                                                 }
                                                 else if(x_antigo>=480&&x_antigo<=690 && y_antigo<=260&&y_antigo>=210)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,45,65,8,10);
                                                    gotoxy(50,8);
                                                    printf("CONSULTAR");
                                                 }
                                                 else if(entra2)
                                                 {
                                                     entra2=false;
                                                    gotoxy(50,8);
                                                    printf("CONSULTAR");
                                                 }



                                                gotoxy(0,25);
                                                if(kbhit())
                                                {
                                                    resp=getch();
                                                }



                                                if(resp==13)
                                                {
                                                    switch(y)
                                                    {
                                                    case 6:
                                                        {


                                                           break;
                                                        }
                                                    case 8:
                                                        {

                                                        }

                                                    }
                                                }

                    }while((Coordenadas.x>=488&&Coordenadas.x<=699 && Coordenadas.y<=260&&Coordenadas.y>=100));

                    Quadro_colorido(7,45,65,3,6);
                    Quadro_colorido(3,45,65,6,10);

                    entra1=entra2=true;

                        textbackground(7);
                        att3++;
                        gotoxy(50,4);
                        printf("PRODUTOS");

                    }
                    else if(att3==0)
                    {
                        textbackground(7);
                        att3++;
                        gotoxy(50,4);
                        printf("PRODUTOS");

                    }

                    //FINANCEIRO
                    if((Coordenadas.x>=700&&Coordenadas.x<=910 && Coordenadas.y<=166&&Coordenadas.y>=100))
                    {   att3=0;


                            Quadro_colorido(8,65,85,3,10);
                            gotoxy(70,4);
                            printf("FINANCEIRO");
                            do
                            {

                                    x_antigo=Coordenadas.x;
                                    y_antigo=Coordenadas.y;
                                    GetCursorPos(&Coordenadas);
                                    gotoxy(2,2);
                                    printf("X: %d Y: %d", Coordenadas.x, Coordenadas.y);

                                                    textbackground(8);
                                                 if((Coordenadas.x>=700&&Coordenadas.x<=910 && Coordenadas.y<=210&&Coordenadas.y>=170))
                                                 {
                                                     y=6;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,65,85,6,8);
                                                        gotoxy(70,6);
                                                        printf("CADASTRAR");
                                                        textbackground(8);
                                                    }


                                                 }
                                                 else if(x_antigo>=700&&x_antigo<=910 && y_antigo<=210&&y_antigo>=170)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,65,85,6,8);
                                                    gotoxy(70,6);
                                                    printf("CADASTRAR");
                                                 }
                                                 else if(entra1)
                                                 {
                                                     entra1=false;
                                                    gotoxy(70,6);
                                                    printf("CADASTRAR");
                                                 }


                                                if((Coordenadas.x>=700&&Coordenadas.x<=910 && Coordenadas.y<=260&&Coordenadas.y>=210))
                                                 {
                                                    y=8;
                                                    ativou++;
                                                    if(ativou==1)
                                                    {
                                                        _beginthread(Musica_opcao,0,NULL);
                                                        textbackground(7);
                                                        Quadro_colorido(7,65,85,8,10);
                                                        gotoxy(70,8);
                                                        printf("CONSULTAR");
                                                        textbackground(8);
                                                    }



                                                 }
                                                 else if(x_antigo>=700&&x_antigo<=910 && y_antigo<=260&&y_antigo>=210)
                                                 {
                                                    ativou=0;
                                                    Quadro_colorido(8,65,85,8,10);
                                                    gotoxy(70,8);
                                                    printf("CONSULTAR");
                                                 }
                                                 else if(entra2)
                                                 {
                                                     entra2=false;
                                                    gotoxy(70,8);
                                                    printf("CONSULTAR");
                                                 }



                                                gotoxy(0,25);
                                                if(kbhit())
                                                {
                                                    resp=getch();
                                                }



                                                if(resp==13)
                                                {
                                                    switch(y)
                                                    {
                                                    case 6:
                                                        {
                                                           break;
                                                        }
                                                    case 8:
                                                        {
                                                            break;
                                                        }

                                                    }
                                                }

                    }while((Coordenadas.x>=700&&Coordenadas.x<=910 && Coordenadas.y<=260&&Coordenadas.y>=100));

                    Quadro_colorido(7,65,85,3,6);
                    Quadro_colorido(3,65,85,6,10);

                    entra1=entra2=true;

                        textbackground(7);
                        att4++;
                        gotoxy(70,4);
                        printf("FINANCEIRO");

                    }
                    else if(att4==0)
                    {
                        textbackground(7);
                        att4++;
                        gotoxy(70,4);
                        printf("FINANCEIRO");

                    }

                    //SAIR
                    if((Coordenadas.x>=910&&Coordenadas.x<=1150 && Coordenadas.y<=166&&Coordenadas.y>=100))
                    {   att5=0;
                            if(entra1)
                            {
                                Quadro_colorido(8,85,105,3,6);
                                gotoxy(90,4);
                                printf("SAIR");
                                entra1=false;
                            }


                            if(resp==13)
                            {
                                if(MessageBox(0,"Deseja sair mesmo?","",4)==6)
                                {
                                    exit(0);
                                    fecha=false;
                                }
                            }
                    }
                    else if(att5==0)
                    {

                            att5++;
                            Quadro_colorido(7,85,105,3,6);
                            gotoxy(90,4);
                            printf("SAIR");
                            entra1=true;
                    }

                            gotoxy(0,26);
                            if(kbhit())
                            {
                                resp=getch();
                            }

				}while(fecha);

}
int Logo(int j, int i)
{      //int j=50,i=1;
        gotoxy(j,i);i++;
        printf("         _\n");
        gotoxy(j,i);i++;
        printf("       _/ \\      _-'\n");
        gotoxy(j,i);i++;
        printf("      _/|  \\-''- _ /\n");
        gotoxy(j,i);i++;
        printf("__-'  { |         \\\n");
        gotoxy(j,i);i++;
        printf("     /             \\\n");
        gotoxy(j,i);i++;
        printf("     /      ''o. |o }\n");
        gotoxy(j,i);i++;
        printf("     |           \\  ;\n");
        gotoxy(j,i);i++;
        printf("                   ',\n");
        gotoxy(j,i);i++;
        printf("        \\_         __ \\\n");
        gotoxy(j,i);i++;
        printf("          ''-_    \\.//\n");
        gotoxy(j,i);i++;
        printf("            / '-____'\n");
        gotoxy(j,i);i++;
        printf("           /\n");
        gotoxy(j,i);i++;
        printf("         _'\n");
        gotoxy(j,i);i++;
        printf("       _-'\n");

}
int Consulta_funcionario(char procura[200])
{
	
    FILE *p;
    int achou=0;
    TFuncionario consulta;
    p=fopen("Funcionario","rb");
    fpos_t Byte;
	
    fread(&consulta,sizeof(consulta),1,p);
    while(!feof(p)&&achou==0)
    {
        if(strcmp(consulta.nome,procura)==0)
        {
            achou=1;
            fgetpos(p,&Byte);
            fclose(p);
            return(Byte);
        }
        fread(&consulta,sizeof(consulta),1,p);
    }
    if(achou==0)
    {

        fclose(p);
        return(0);
    }
}
int Consulta_user(char pesquisa[200])
{

	FILE* pesquisar;
	TUsuario usuario;
	int resultado;
	int find=0;
	fpos_t Byte;
	
	pesquisar=fopen("Usuario","rb");
	fread(&usuario,sizeof(usuario),1,pesquisar);

    while(!feof(pesquisar)&&find==0)
	{
		
		if(strcmp(usuario.nome,pesquisa)==0)
		{
			
			find=1;
			fgetpos(pesquisar,&Byte);
			fclose(pesquisar);
			return (Byte);
		}
		fread(&usuario,sizeof(usuario),1,pesquisar);
	}
	if(find==0)
    {
        fclose(pesquisar);
        return(0);
    }

}
int Aba_Consulta(int arquivo /*1 - funcionarios ; 2- usuarios; 3- produtos*/)
{
    POINT Coordenadas;
    bool entra=true;
    int ativou=0;
    char resp;
    system("cls");
    system("Color 3F");
    Logo(50,1);
    Janela();
    //Borda
    {
     int i,j;

        gotoxy(34,17);
        printf("%c",186);

    gotoxy(34,18);
    printf("%c",200);
    gotoxy(34,16);
    printf("%c",201);
    for(i=35;i<85;i++)
    {
        gotoxy(i,16);
        printf("%c",205);
        gotoxy(i,18);
        printf("%c",205);
    }

    gotoxy(85,18);
    printf("%c",188);

    gotoxy(85,16);
    printf("%c",187);

        gotoxy(85,17);
        printf("%c",186);

    }
    textbackground(15);
    int i;

    for(i=35;i<85;i++)
    {
        gotoxy(i,17);
        printf(" ");
    }


    do
    {
        if(kbhit())
        {
            resp=getch();

            if(resp==13 && Coordenadas.x<940 &&Coordenadas.x>388 &&Coordenadas.y<470 && Coordenadas.y>423)
            {
                textbackground(15);
                textcolor(8);
                char procura[200];
                char pesquisa[200];
                gotoxy(35,17);
                gets(procura);
                fflush(stdin);
                resp=' ';

                fpos_t Byte;


                if(arquivo==1)
                {
                    Byte=Consulta_funcionario(procura);
                }
                 if(arquivo==2)
                {
                    Byte=Consulta_user(procura);
                    
                }

                    if(Byte!=0)
                    {
                        Aba_Resultado(Byte,arquivo);
                        break;
                    }
                    else
                    {
                        gotoxy(40,18);
                        printf("Resultado Nao encontrado");
                        Sleep(1000);
                        Aba_Consulta(arquivo);
                        break;
                    }

            }
    
        }


        textbackground(3);
        gotoxy(20,10);
        printf("x: %d  y: %d ",Coordenadas.x,Coordenadas.y);
        GetCursorPos(&Coordenadas);
        if(Coordenadas.x<775 &&Coordenadas.x>555 &&Coordenadas.y<580 && Coordenadas.y>510)
        {
            if(ativou==0)
            {   textcolor(15);
                Quadro_colorido(8,50,70,20,23);
                gotoxy(56,21);
                printf("RETORNAR");
            }
            entra=true;
            ativou++;
            if(resp==13)
            {
                resp=' ';
                break;
            }

        }
        else if(entra)
        {
            Quadro_colorido(7,50,70,20,23);
            textcolor(8);
            gotoxy(56,21);
            printf("RETORNAR");
            entra=false;
            ativou=0;
        }
       
    

    }while(1);



}
void Resultado_Funcionario(fpos_t Byte)
{
    FILE*p;
    TFuncionario funcionario;
    p=fopen("Funcionario","rb");
    Byte=Byte-sizeof(funcionario);
    fsetpos(p,&Byte);
    fread(&funcionario,sizeof(funcionario),1,p);

    gotoxy(40,6);
    printf("NOME: %s",funcionario.nome);

    gotoxy(40,8);
    printf("DATA DE NASCIMENTO: %s ",funcionario.data_nascimento);

    gotoxy(40,10);
    printf("RG: %i",funcionario.RG);

    gotoxy(60,10);
    printf("CPF: %i",funcionario.CPF);

    gotoxy(40,12);
    printf("E-MAIL: %s ",funcionario.email);

    gotoxy(40,14);
    printf("TELEFONE: %i",funcionario.telefone);

    gotoxy(40,16);
    printf("ENDERECO: %s ",funcionario.endereco);

    gotoxy(40,18);
    printf("SALARIO: %f ",funcionario.salario);

    gotoxy(40,20);
    printf("USUARIO: %s",funcionario.usuario);


    fclose(p);


}
void Resultado_Usuario(fpos_t Byte)
{	
	FILE* pesquisar;
    TFuncionario usuario;
    pesquisar=fopen("Usuario","rb");
    Byte=Byte-sizeof(usuario);
    fsetpos(pesquisar,&Byte);
    fread(&usuario,sizeof(usuario),1,pesquisar);
	
	
    gotoxy(40,6);
    printf("NOME: ",usuario.nome);

    gotoxy(40,7);
    printf("USUARIO: %s",usuario.usuario);

    fclose(pesquisar);

}
int Aba_Resultado(fpos_t Byte,int arquivo/*1 - funcionarios ; 2- usuarios; 3- produtos*/)
{
    system("cls");
    Janela();
    system("Color 3F");

    POINT Coordenadas;
    bool button1=true,button2=true,button3=true,button4=true;
    int ativado=0;

    Logo(4,1);
    Quadro_colorido(15,38,90,4,25);

    textcolor(3);

    //desenho da borda da janela
    {
    int i,j;
    for(i=5;i<25;i++)
    {
        gotoxy(38,i);
        printf("%c",186);
    }
    gotoxy(38,25);
    printf("%c",200);
    gotoxy(38,4);
    printf("%c",201);
    for(i=39;i<90;i++)
    {
        gotoxy(i,4);
        printf("%c",205);
    }
    for(i=39;i<90;i++)
    {
        gotoxy(i,25);
        printf("%c",205);
    }

    gotoxy(90,25);
    printf("%c",188);

    gotoxy(90,4);
    printf("%c",187);
    for(i=5;i<25;i++)
    {
        gotoxy(90,i);
        printf("%c",186);
    }
}

    if(arquivo==1)
    {   textbackground(3);
        textcolor(15);
        gotoxy(58,2);
        printf("FUNCIONARIO");
        textcolor(8);
        textbackground(15);
        Resultado_Funcionario(Byte);
    }
    if(arquivo==2)
    { 
		textbackground(3);
        textcolor(15);
        gotoxy(58,2);
        printf("USUARIO");
        textcolor(8);
        textbackground(15);
        Resultado_Usuario(Byte);
    }
    if(arquivo==3)
    {   textbackground(3);
        textcolor(15);
        gotoxy(58,2);
        printf("PRODUTO");
        textcolor(8);
        textbackground(15);
       // Resultado_Produto(Byte);
    }

    char resp;
    do
    {
        if(kbhit())
        {
            resp=getch();
        }
        gotoxy(50,10);
        printf("x: %d  y: %d ",Coordenadas.x,Coordenadas.y);
        GetCursorPos(&Coordenadas);

        if(Coordenadas.x>1050&&Coordenadas.x<1300 &&Coordenadas.y>150&&Coordenadas.y<220)
        {
            if(ativado==0)
            {
                Quadro_colorido(8,95,118,5,8);
                gotoxy(103,6);
                textcolor(15);
                printf("EDITAR");
            }
            button1=true;
            ativado++;
            if(resp==13)
            {
                resp=' ';
            }

        }
        else if(button1)
        {
            button1=false;
            Quadro_colorido(7,95,118,5,8);
            gotoxy(103,6);
            textcolor(8);
            printf("EDITAR");
            ativado=0;
        }
        if(Coordenadas.x>1050&&Coordenadas.x<1300 &&Coordenadas.y>270&&Coordenadas.y<340)
        {
            if(ativado==0)
            {
                Quadro_colorido(8,95,118,10,13);
                gotoxy(102,11);
                textcolor(15);
                printf("DESATIVAR");
            }
            button2=true;
            ativado++;

            if(resp==13)
            {
                resp=' ';
            }

        }
        else if(button2)
        {
            button2=false;
            Quadro_colorido(7,95,118,10,13);
            gotoxy(102,11);
            textcolor(8);
            printf("DESATIVAR");
            ativado=0;
        }

        if(Coordenadas.x>1050&&Coordenadas.x<1300 &&Coordenadas.y>390&&Coordenadas.y<450)
        {
            if(ativado==0)
            {
                Quadro_colorido(8,95,118,15,18);
                gotoxy(102,16);
                textcolor(15);
                printf("RETORNAR");

            }
            button3=true;
            ativado++;
            if(resp==13)
            {
                Aba_Consulta(arquivo);
                resp=' ';
                break;
            }

        }
        else if(button3)
        {
            button3=false;
            Quadro_colorido(7,95,118,15,18);
            gotoxy(102,16);
            textcolor(8);
            printf("RETORNAR");
            ativado=0;
        }

        if(Coordenadas.x>1050&&Coordenadas.x<1300 &&Coordenadas.y>510&&Coordenadas.y<580)
        {
            if(ativado==0)
            {
                Quadro_colorido(8,95,118,20,23);
                gotoxy(100,21);
                textcolor(15);
                printf("MENU PRINCIPAL");
            }
            button4=true;
            ativado++;
            if(resp==13)
            {
                resp=' ';
                break;
            }

        }
        else if(button4)
        {
            button4=false;
            Quadro_colorido(7,95,118,20,23);
            gotoxy(100,21);
            textcolor(8);
            printf("MENU PRINCIPAL");
            ativado=0;
        }



    }while(1);

}
int Aba_Cadastro(int arquivo /*1-Funcionario; 2-usuarios 3- produtos*/)
{
    system("cls");
    Janela();
    system("Color 3F");

    Logo(4,1);
    gotoxy(50,2);
    printf("CADASTRAR FUNCIONARIO");
    Quadro_colorido(15,38,90,4,25);

    textcolor(3);

    //desenho da borda da janela
    {
    int i,j;
    for(i=5;i<25;i++)
    {
        gotoxy(38,i);
        printf("%c",186);
    }
    gotoxy(38,25);
    printf("%c",200);
    gotoxy(38,4);
    printf("%c",201);
    for(i=39;i<90;i++)
    {
        gotoxy(i,4);
        printf("%c",205);
    }
    for(i=39;i<90;i++)
    {
        gotoxy(i,25);
        printf("%c",205);
    }

    gotoxy(90,25);
    printf("%c",188);

    gotoxy(90,4);
    printf("%c",187);
    for(i=5;i<25;i++)
    {
        gotoxy(90,i);
        printf("%c",186);
    }
}

    if(arquivo==1)
    {
        if(Cadastro_funcionario()==1)
        {
            MessageBox(0,"CADASTRO REALIZADO COM SUCESSO!","CADASTRO DE FUNCIONARIO",0);
        }
        else
        {
            Aba_Cadastro(arquivo);
        }
    }
    if(arquivo==2)
    {
        if(Cadastro_usuario()==2)
        {
            MessageBox(0,"CADASTRO REALIZADO COM SUCESSO!","CADASTRO DE USUARIO",0);
        }
        else
        {
            Aba_Cadastro(arquivo);
        }
    }
}
int Cadastro_funcionario()
{
    TFuncionario funcionario;

    FILE* p;
    p=fopen("Funcionario","rb");

    if(p==NULL)
    {
        p=fopen("Funcionario","wb");

    }

    gotoxy(40,5);
    printf("NOME:");

    gotoxy(40,7);
    printf("DATA DE NASCIMENTO:");

    gotoxy(40,9);
    printf("RG:");

    gotoxy(60,9);
    printf("CPF:");

    gotoxy(40,11);
    printf("E-MAIL:");

    gotoxy(40,13);
    printf("TELEFONE:");

    gotoxy(40,15);
    printf("ENDERECO:");

    gotoxy(40,17);
    printf("SALARIO:");

    gotoxy(40,19);
    printf("USUARIO:");

    gotoxy(40,21);
    printf("SENHA:");

    p=fopen("Funcionario","ab+");

    gotoxy(45,5);
    gets(funcionario.nome);

    if(Consulta_funcionario(funcionario.nome)!=0)
    {
        gotoxy(45,6);
        textcolor(4);
        printf("Funcionario ja cadastrado!");
        textcolor(0);

        Sleep(1000);
        fclose(p);
        return(0);
    }
    else
    {    int i,j=60;
            //DATA DE NASCIMENTO
            for(i=0;i<10;i++)
            {

                gotoxy(j,7);
                if(i==2 || i==5)
                {
                    funcionario.data_nascimento[i]= '/';

                }
                else
                {
                    funcionario.data_nascimento[i]=getch();

                    if(funcionario.data_nascimento[i]==8)
                    {
                        if(i>=0)
                        {
                            i--;
                            if(i==5||i==2)
                            {
                                i--;
                                j--;

                            }
                            funcionario.data_nascimento[i]=' ';
                            gotoxy(j-1,7);
                            printf("%c",funcionario.data_nascimento[i]);

                            funcionario.data_nascimento[i]=' ';
                            i--;
                            j-=2;

                        }
                        else
                        {
                            i=-1;
                        }
                    }

                    /*if(funcionario.data_nascimento[i]<30 ||funcionario.data_nascimento[i]>39)
                    {
                        funcionario.data_nascimento[i]=' ';
                        i--;
                        j--;
                    }*/

                }
                if(j<60)
                {
                    j=60;
                }
                gotoxy(j,7);
                printf("%c",funcionario.data_nascimento[i]);
                if(i>=0)
                {
                    j++;
                }



            }
            //RG
            j=45;
            for(i=0;i<9;i++)
            {   gotoxy(j,9);
                if(i==1||i==5)
                {
                    funcionario.RG[i]='.';
                }
                else
                {
                    funcionario.RG[i]=getch();

                    if(funcionario.RG[i]==8)
                    {
                        if(i>0)
                        {
                            funcionario.RG[i]=' ';
                            gotoxy(j,9);
                            printf("%c",funcionario.RG[i]);
                            i--;
                            if(i==5||i==1)
                            {
                                i--;
                                j--;

                            }
                            funcionario.RG[i]=' ';
                            gotoxy(j-1,9);
                            printf("%c",funcionario.RG[i]);

                            i--;
                            j-=2;
                        }
                        else
                        {
                            i--;
                        }
                    }
                   /* else if(funcionario.RG[i]<30 ||funcionario.RG[i]>39)
                    {
                        funcionario.RG[i]=' ';
                        i--;
                        j--;
                    }*/
                }

                if(j>=45)
                {
                    gotoxy(j,9);
                    printf("%c",funcionario.RG[i]);
                }

                if(j<45)
                {
                    j=45;
                }
                if(i>=0)
                {
                    j++;

                }




            }

            //CPF
            j=65;
            for(i=0;i<14;i++)
            {
                gotoxy(j,9);
                if(i==3||i==7)
                {
                    funcionario.CPF[i]='.';

                }
                else if(i==11)
                {
                    funcionario.CPF[i]='-';

                }
                else
                {
                    funcionario.CPF[i]=getch();

                    if(funcionario.CPF[i]==8)
                    {
                        if(i>0)
                        {
                            funcionario.CPF[i]=' ';
                            gotoxy(j,9);
                            printf("%c",funcionario.CPF[i]);
                            i--;
                            if(i==3||i==7||i==11)
                            {
                                i--;
                                j--;

                            }
                            funcionario.CPF[i]=' ';
                            gotoxy(j-1,9);
                            printf("%c",funcionario.CPF[i]);

                            i--;
                            j-=2;
                        }
                        else
                        {
                            i--;
                        }
                    }
                    /*else if(funcionario.CPF[i]<30 ||funcionario.CPF[i]>39)
                    {
                        funcionario.CPF[i]=' ';
                        i--;
                        j--;
                    }*/
                }

                if(j>=65)
                {
                    gotoxy(j,9);
                    printf("%c",funcionario.CPF[i]);
                }

                if(j<65)
                {
                    j=65;
                }
                if(i>=0)
                {
                    j++;

                }
                }

            //EMAIL
            gotoxy(47,11);
            gets(funcionario.email);
            fflush(stdin);


            //TELEFONE
            j=50;
            for(i=0;i<13;i++)
            {   gotoxy(j,13);
                if(i==0)
                {
                    funcionario.telefone[i]='(';
                }
                else if(i==3)
                {
                    funcionario.telefone[i]=')';
                }
                else
                {

                    funcionario.telefone[i]=getch();
                    if(funcionario.telefone[i]==13)
                    {
                        break;
                    }
                    else if(funcionario.telefone[i]==8)
                    {
                        if(i>0)
                        {
                            funcionario.telefone[i]=' ';
                            gotoxy(j,13);
                            printf("%c",funcionario.telefone[i]);
                            i--;
                            if(i==0||i==3)
                            {
                                i--;
                                j--;

                            }
                            funcionario.telefone[i]=' ';
                            gotoxy(j-1,13);
                            printf("%c",funcionario.telefone[i]);

                            i--;
                            j-=2;
                        }
                        else
                        {
                            i--;
                        }
                    }
                    /*else if(funcionario.telefone[i]<30 ||funcionario.telefone[i]>39)
                    {
                        funcionario.telefone[i]=' ';
                        i--;
                        j--;
                    }*/
                }

                if(j>=50)
                {
                    gotoxy(j,13);
                    printf("%c",funcionario.telefone[i]);
                }

                if(j<50)
                {
                    j=50;
                }
                if(i>=0)
                {
                    j++;

                }
                }



            //ENDERE�O
            gotoxy(50,15);
            gets(funcionario.endereco);
            fflush(stdin);

            //SALARIO
            gotoxy(49,17);
            printf("R$");
            gotoxy(51,17);
            scanf("%f",&funcionario.salario);
            fflush(stdin);

            //USUARIO
            gotoxy(49,19);
            gets(funcionario.usuario);

            fflush(stdin);

            //SENHA
            gotoxy(49,21);
            gets(funcionario.senha);

            fwrite(&funcionario,sizeof(funcionario),1,p);
            fclose(p);

            return(1);
    }


}
int Cadastro_usuario()
{
	TUsuario usuario;
	FILE *p;
	p = fopen("Usuario", "rb");
	if (p == NULL)
	{
		p = fopen("Usuario", "wb");
	}
	gotoxy(40,5);
    printf("NOME:");

    gotoxy(40,7);
    printf("USUARIO:");

    gotoxy(40,9);
    printf("SENHA:");


	p=fopen("Usuario","ab+");


    gotoxy(45,5);
	fflush(stdin);
    gets(usuario.nome);


    if(Consulta_user(usuario.nome)!=0)
    {
        gotoxy(45,6);
        textcolor(4);
        printf("Usuario ja cadastrado!");
        textcolor(0);

        Sleep(1000);
        fclose(p);
        return(0);
    }
    else
	{
			
            //USUARIO
            gotoxy(49,7);
			fflush(stdin);
		    gets(usuario.usuario);

            //SENHA
            gotoxy(49,9);
			fflush(stdin);
		    gets(usuario.senha);

            fwrite(&usuario,sizeof(usuario),1,p);
            fclose(p);
            return(2);
    }
}


//Define o tamanho da tela com chamada do sistema
void redefine(void){
    system("color 3F"); //Padroniza a cor do sistema.
    system("MODE con cols=123 lines=28"); //Redimensiona a tela
    system("title STEAM - by: LGMT"); //D� t�tulo a tela
}
int main()
{
    redefine();

    FILE*p;
    p=fopen("Funcionario","rb");
    fclose(p);
    if(p==NULL)
    {
        if(MessageBox(0,"BEM VINDO AO PROGRAMA!\nAntes de come�ar a utiliza-lo � necessario que leia e aceite os termos de uso.\n","TERMOS DE USO",4)==6)
        {   //pra cadastrar o admin quando usa pela primeira vez
            Aba_Cadastro(1);
            p = fopen("Usuario", "wb");
            main();
        }
        else
        {
            exit(0);
        }

    }
    else
    {
       // Tela_de_Carregamento();

        //Login();

       Area_de_Trabalho();

    }

}

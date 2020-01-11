main()
{
    int LS, incremento, variavel_controladora;

    printf("Digite o Limite superior: ");
    scanf("%i", &LS);
    printf("Digite o incremento: ");
    scanf("%i", &incremento);

    for(variavel_controladora=0; variavel_controladora<=LS; variavel_controladora=variavel_controladora+incremento)
    {
        printf("%i ", variavel_controladora);
    }
}

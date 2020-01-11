main()
{
    int i,j,y=0;

    for(i=0;i<=2;i++)
    {
        for(j=0;j<y; j++)
        {
            if(j==i)
            {
                printf("\t");
            }
            else
            {
                printf("%i-%i\t", i,j);

            }
        }
        printf("\n");
        y++;

    }
}

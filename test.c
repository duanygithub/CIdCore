int main()
{
    int a = 111, *p = &a, **p1 = &p;
    printf("%d %d %d", a, *p, **p1);
    return 0;
}
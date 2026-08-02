import { createClient } from 'jsr:@supabase/supabase-js@2'

Deno.serve(async (req) => {
  try {
    const { user_id, amount } = await req.json()

    if (!user_id || typeof amount !== 'number') {
      return new Response(JSON.stringify({ error: 'invalid payload' }), { status: 400 })
    }

    const supabaseAdmin = createClient(
      Deno.env.get('SUPABASE_URL')!,
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!
    )

    const { data: profile } = await supabaseAdmin
      .from('profiles')
      .select('n_coins_balance')
      .eq('id', user_id)
      .single()

    const newBalance = (profile?.n_coins_balance ?? 0) + amount

    await supabaseAdmin
      .from('profiles')
      .update({ n_coins_balance: newBalance })
      .eq('id', user_id)

    return new Response(JSON.stringify({ newBalance }), {
      headers: { 'Content-Type': 'application/json' }
    })
  } catch (err) {
    return new Response(JSON.stringify({ error: String(err) }), { status: 500 })
  }
})

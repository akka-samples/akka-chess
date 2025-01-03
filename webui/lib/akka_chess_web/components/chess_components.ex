defmodule AkkaChessWeb.ChessComponents do
  use Phoenix.Component

  require Integer

  attr :board, :list, required: true

  def chessboard(assigns) do
    ~H"""
    <div class="grid grid-cols-9">
      <div
        :for={letter <- [" ", "A", "B", "C", "D", "E", "F", "G", "H"]}
        class="text-l text-center text-slate-400"
        style="padding-bottom: 1.5rem;"
      >
        {letter}
      </div>
      <%= for {row, ridx} <- Enum.chunk_every(@board.pieces, 8) |> Enum.with_index() do %>
        <div class="text-center align-middle p-1 items-center align-center text-slate-400">
          {8 - ridx}
        </div>
        <div
          :for={{piece, idx} <- Enum.with_index(row)}
          style={get_bg(ridx, idx)}
          class={get_border(ridx, idx) <> " text-5xl text-center align-middle p-1 items-center align-center place-content-center"}
        >
          <.chesspiece piece={piece} idx={idx} />
        </div>
      <% end %>
    </div>
    """
  end

  attr :piece, :any, required: true
  attr :idx, :integer, required: true

  def chesspiece(%{piece: 0} = assigns) do
    ~H"""
    <br />
    """
  end

  def chesspiece(%{piece: piece} = assigns) do
    assigns = assign(assigns, :piece, piece)

    ~H"""
    {@piece}
    """
  end

  defp get_border(row, col) do
    {row, col}
    |> put_top_border()
    |> put_left_border()
    |> put_right_border()
    |> put_bottom_border()
    |> put_border_color()
  end

  defp put_top_border({row, col}) when row == 0 do
    {row, col, " border-t-2"}
  end

  defp put_top_border({row, col}), do: {row, col, ""}

  defp put_left_border({row, col, totalstr}) when col == 0,
    do: {row, col, totalstr <> " border-l-2"}

  defp put_left_border({row, col, totalstr}), do: {row, col, totalstr}

  defp put_right_border({row, col, totalstr}) when col == 7,
    do: {row, col, totalstr <> " border-r-2"}

  defp put_right_border({row, col, totalstr}), do: {row, col, totalstr}

  defp put_bottom_border({row, col, totalStr}) when row == 7,
    do: {row, col, totalStr <> " border-b-2"}

  defp put_bottom_border({row, col, totalstr}), do: {row, col, totalstr}

  defp put_border_color({_, _, totalstr}), do: totalstr <> " border-black"

  defp is_bg?(row, col) do
    if Integer.is_even(row) do
      !Integer.is_even(col)
    else
      Integer.is_even(col)
    end
  end

  defp get_bg(row, col) do
    if is_bg?(row, col) do
      "background-color: rgb(147 197 253);"
    else
      "background-color: rgb(255 255 255);"
    end
  end
end

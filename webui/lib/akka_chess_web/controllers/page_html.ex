defmodule AkkaChessWeb.PageHTML do
  @moduledoc """
  This module contains pages rendered by PageController.

  See the `page_html` directory for all templates available.
  """
  use AkkaChessWeb, :html

  import SaladUI.Button
  import SaladUI.Card

  embed_templates "page_html/*"
end

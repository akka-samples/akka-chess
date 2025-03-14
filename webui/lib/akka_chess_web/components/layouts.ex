defmodule AkkaChessWeb.Layouts do
  @moduledoc """
  This module holds different layouts used by your application.

  See the `layouts` directory for all templates available.
  The "root" layout is a skeleton rendered as part of the
  application router. The "app" layout is set as the default
  layout on both `use AkkaChessWeb, :controller` and
  `use AkkaChessWeb, :live_view`.
  """
  use AkkaChessWeb, :html

  import SaladUI.Tooltip
  import SaladUI.DropdownMenu
  import SaladUI.Menu
  import SaladUI.Button

  embed_templates "layouts/*"
end
